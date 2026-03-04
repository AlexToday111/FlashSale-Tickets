package com.ticketing.order.infra.messaging;

import com.ticketing.contr.events.EventEnvelope;
import com.ticketing.contr.events.PaymentFailedPayload;
import com.ticketing.contr.events.PaymentSucceededPayload;
import com.ticketing.order.Api.model.OrderEntity;
import com.ticketing.order.Api.model.ProcessedEventEntity;
import com.ticketing.order.Api.repository.OrderRepository;
import com.ticketing.order.Api.repository.ProcessedEventRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentResultListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultListener.class);

    private static final String SUCCEEDED_CONSUMER = "payment-succeeded-consumer";
    private static final String FAILED_CONSUMER = "payment-failed-consumer";

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final Counter processedCounter;
    private final Counter failedCounter;

    public PaymentResultListener(OrderRepository orderRepository,
                                 ProcessedEventRepository processedEventRepository,
                                 MeterRegistry meterRegistry) {
        this.orderRepository = orderRepository;
        this.processedEventRepository = processedEventRepository;
        this.processedCounter = meterRegistry.counter("order.consumer.processed");
        this.failedCounter = meterRegistry.counter("order.consumer.failed");
    }

    @Transactional
    @RabbitListener(queues = "payments.succeeded.queue")
    public void handleSucceeded(EventEnvelope<PaymentSucceededPayload> event) {
        handleResultEvent(event.eventId(), event.payload().orderId(), OrderEntity.Status.PAID, SUCCEEDED_CONSUMER);
    }

    @Transactional
    @RabbitListener(queues = "payments.failed.queue")
    public void handleFailed(EventEnvelope<PaymentFailedPayload> event) {
        handleResultEvent(event.eventId(), event.payload().orderId(), OrderEntity.Status.CANCELLED, FAILED_CONSUMER);
    }

    private void handleResultEvent(UUID eventId, UUID orderId, OrderEntity.Status targetStatus, String consumerName) {
        if (processedEventRepository.existsById(eventId)) {
            log.info("Duplicate payment result eventId={}, consumer={}, skipping", eventId, consumerName);
            return;
        }

        try {
            Optional<OrderEntity> maybeOrder = orderRepository.findById(orderId);
            if (maybeOrder.isEmpty()) {
                log.warn("Order not found for payment result, orderId={}, eventId={}", orderId, eventId);
                return;
            }

            OrderEntity order = maybeOrder.get();
            if (order.getStatus() != OrderEntity.Status.PENDING_PAYMENT) {
                log.info("Ignoring payment result for orderId={} with status={}, eventId={}",
                        orderId, order.getStatus(), eventId);
            } else {
                order.setStatus(targetStatus);
                orderRepository.save(order);
                log.info("Order status changed orderId={} newStatus={} eventId={}", orderId, targetStatus, eventId);
            }

            ProcessedEventEntity processed = new ProcessedEventEntity();
            processed.setEventId(eventId);
            processed.setProcessedAt(Instant.now());
            processed.setConsumerName(consumerName);
            processedEventRepository.save(processed);

            processedCounter.increment();
        } catch (RuntimeException ex) {
            failedCounter.increment();
            log.error("Error processing payment result eventId={}, orderId={}", eventId, orderId, ex);
            throw ex;
        }
    }
}

