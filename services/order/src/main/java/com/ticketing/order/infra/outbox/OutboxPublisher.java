package com.ticketing.order.infra.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.contr.events.EventEnvelope;
import com.ticketing.order.Api.model.OutboxEntity;
import com.ticketing.order.Api.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final int batchSize;
    private final int maxAttempts;

    public OutboxPublisher(OutboxRepository outboxRepository,
                           RabbitTemplate rabbitTemplate,
                           ObjectMapper objectMapper,
                           @Value("${order.outbox.batch-size:50}") int batchSize,
                           @Value("${order.outbox.max-attempts:5}") int maxAttempts) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${order.outbox.poll-interval-ms:5000}")
    @Transactional
    public void publishPending() {
        List<OutboxEntity> batch = outboxRepository.findTopBatchForUpdate(
                OutboxEntity.Status.NEW,
                PageRequest.of(0, batchSize)
        );

        if (batch.isEmpty()) {
            return;
        }

        log.debug("Processing outbox batch size={}", batch.size());

        for (OutboxEntity record : batch) {
            try {
                EventEnvelope<?> envelope = objectMapper.readValue(record.getPayload(), EventEnvelope.class);

                // Для MVP: роутинг по типу события
                String exchange = "payments.exchange";
                String routingKey = "payments.requested";

                rabbitTemplate.convertAndSend(exchange, routingKey, envelope);

                record.setStatus(OutboxEntity.Status.SENT);
                record.setSentAt(Instant.now());
                record.setLastError(null);
            } catch (Exception ex) {
                log.error("Failed to publish outbox record id={}", record.getId(), ex);
                record.setAttempts(record.getAttempts() + 1);
                record.setLastError(truncateError(ex));

                if (record.getAttempts() >= maxAttempts) {
                    record.setStatus(OutboxEntity.Status.FAILED);
                }
            }
        }
    }

    private String truncateError(Exception ex) {
        try {
            String asString = objectMapper.writeValueAsString(ex.getMessage());
            return asString.length() > 2000 ? asString.substring(0, 2000) : asString;
        } catch (JsonProcessingException e) {
            String msg = ex.getMessage();
            return msg != null && msg.length() > 2000 ? msg.substring(0, 2000) : msg;
        }
    }
}

