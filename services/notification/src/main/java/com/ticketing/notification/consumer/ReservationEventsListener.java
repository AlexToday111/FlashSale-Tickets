package com.ticketing.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.notification.config.messaging.RabbitConfig;
import com.ticketing.notification.service.NotificationService;
import com.ticketing.reservation.api.ReservationEventBindings;
import com.ticketing.reservation.api.dto.ReservationCancelled;
import com.ticketing.reservation.api.dto.ReservationCreated;
import com.ticketing.reservation.api.dto.ReservationExpired;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventsListener {

    private final NotificationService notificationService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final Counter processedCounter;
    private final Counter failuresCounter;
    private final Counter dlqCounter;

    public ReservationEventsListener(NotificationService notificationService,
                                     RabbitTemplate rabbitTemplate,
                                     ObjectMapper objectMapper,
                                     MeterRegistry meterRegistry) {
        this.notificationService = notificationService;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.processedCounter = meterRegistry.counter("processed_total");
        this.failuresCounter = meterRegistry.counter("failures_total");
        this.dlqCounter = meterRegistry.counter("dlq_total");
    }

    @RabbitListener(queues = RabbitConfig.Q_MAIN, containerFactory = "rabbitListenerContainerFactory")
    public void handle(Message message) {
        String eventType = (String) message.getMessageProperties().getHeaders().get("eventType");

        if (eventType == null || eventType.isBlank()) {
            failuresCounter.increment();
            sendToDlq(message);
            return;
        }

        try {
            switch (eventType) {
                case "ReservationCreated" -> {
                    ReservationCreated event = objectMapper.readValue(message.getBody(), ReservationCreated.class);
                    notificationService.handleReservationCreated(event);
                }
                case "ReservationCancelled" -> {
                    ReservationCancelled event = objectMapper.readValue(message.getBody(), ReservationCancelled.class);
                    notificationService.handleReservationCancelled(event);
                }
                case "ReservationExpired" -> {
                    ReservationExpired event = objectMapper.readValue(message.getBody(), ReservationExpired.class);
                    notificationService.handleReservationExpired(event);
                }
                default -> {
                    failuresCounter.increment();
                    sendToDlq(message);
                    return;
                }
            }
            processedCounter.increment();
        } catch (Exception e) {
            failuresCounter.increment();
            handleRetryOrDlq(message);
        }
    }

    private void handleRetryOrDlq(Message originalMessage) {
        int retryCount = getRetryCount(originalMessage);
        if (retryCount < RabbitConfig.MAX_RETRIES) {
            Message toRetry = MessageBuilder.fromMessage(originalMessage)
                    .setHeader("x-retry-count", retryCount + 1)
                    .build();
            rabbitTemplate.send(
                    ReservationEventBindings.EXCHANGE,
                    ReservationEventBindings.Q_NOTIFICATION_RETRY,
                    toRetry
            );
            return;
        }
        sendToDlq(originalMessage);
    }

    private void sendToDlq(Message message) {
        rabbitTemplate.send(
                ReservationEventBindings.EXCHANGE,
                ReservationEventBindings.Q_NOTIFICATION_DLQ,
                message
        );
        dlqCounter.increment();
    }

    private int getRetryCount(Message message) {
        Object count = message.getMessageProperties().getHeaders().get("x-retry-count");
        if (count instanceof Integer i) return i;
        if (count instanceof Long l) return l.intValue();
        if (count instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }
}
