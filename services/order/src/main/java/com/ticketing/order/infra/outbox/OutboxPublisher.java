package com.ticketing.order.infra.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.contr.events.EventEnvelope;
import com.ticketing.contr.events.EventTypes;
import com.ticketing.order.Api.model.OutboxEntity;
import com.ticketing.order.Api.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

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
    private final Counter publishedCounter;
    private final Counter publishErrorCounter;

    public OutboxPublisher(OutboxRepository outboxRepository,
                           RabbitTemplate rabbitTemplate,
                           ObjectMapper objectMapper,
                           MeterRegistry meterRegistry,
                           @Value("${order.outbox.batch-size:50}") int batchSize,
                           @Value("${order.outbox.max-attempts:5}") int maxAttempts) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;

        this.publishedCounter = meterRegistry.counter("order.outbox.published");
        this.publishErrorCounter = meterRegistry.counter("order.outbox.publish.errors");

        Gauge.builder("order.outbox.pending", outboxRepository,
                        repo -> repo.countByStatus(OutboxEntity.Status.NEW))
                .description("Number of NEW outbox records")
                .register(meterRegistry);
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

                String eventType = envelope.eventType();
                String exchange = resolveExchange(eventType);
                String routingKey = resolveRoutingKey(eventType);

                rabbitTemplate.convertAndSend(exchange, routingKey, envelope);

                record.setStatus(OutboxEntity.Status.SENT);
                record.setSentAt(Instant.now());
                record.setLastError(null);
                publishedCounter.increment();
            } catch (Exception ex) {
                log.error("Failed to publish outbox record id={}", record.getId(), ex);
                record.setAttempts(record.getAttempts() + 1);
                record.setLastError(truncateError(ex));
                publishErrorCounter.increment();

                if (record.getAttempts() >= maxAttempts) {
                    record.setStatus(OutboxEntity.Status.FAILED);
                }
            }
        }
    }

    private String resolveExchange(String eventType) {
        if (EventTypes.PAYMENT_REQUESTED.equals(eventType)
                || EventTypes.PAYMENT_SUCCEEDED.equals(eventType)
                || EventTypes.PAYMENT_FAILED.equals(eventType)) {
            return "payments.exchange";
        }
        // fallback, можно логировать unknown
        return "payments.exchange";
    }

    private String resolveRoutingKey(String eventType) {
        if (EventTypes.PAYMENT_REQUESTED.equals(eventType)) {
            return "payments.requested";
        }
        if (EventTypes.PAYMENT_SUCCEEDED.equals(eventType)) {
            return "payments.succeeded";
        }
        if (EventTypes.PAYMENT_FAILED.equals(eventType)) {
            return "payments.failed";
        }
        return "payments.unknown";
    }

    private String truncateError(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null) {
            msg = ex.toString();
        }
        return msg.length() > 2000 ? msg.substring(0, 2000) : msg;
    }
}

