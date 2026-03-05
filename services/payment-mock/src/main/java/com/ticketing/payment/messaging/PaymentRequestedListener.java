package com.ticketing.payment.messaging;

import com.ticketing.contr.events.EventEnvelope;
import com.ticketing.contr.events.EventTypes;
import com.ticketing.contr.events.PaymentFailedPayload;
import com.ticketing.contr.events.PaymentRequestedPayload;
import com.ticketing.contr.events.PaymentSucceededPayload;
import com.ticketing.payment.config.RabbitPaymentConfig;
import com.ticketing.payment.idempotency.ProcessedEventsRepository;
import com.ticketing.payment.model.ProcessedEvents;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Component
public class PaymentRequestedListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentRequestedListener.class);
    private static final String CONSUMER_NAME = "payment-requested-consumer";

    private final ProcessedEventsRepository processedEventsRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();
    private final Counter processedCounter;
    private final Counter failedCounter;
    private final Counter duplicateCounter;
    private final Counter paymentSuccessCounter;
    private final Counter paymentFailureCounter;

    public PaymentRequestedListener(ProcessedEventsRepository processedEventsRepository,
                                    RabbitTemplate rabbitTemplate,
                                    MeterRegistry meterRegistry) {
        this.processedEventsRepository = processedEventsRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.processedCounter = meterRegistry.counter("payment_mock.consumer.processed", "consumer", CONSUMER_NAME);
        this.failedCounter = meterRegistry.counter("payment_mock.consumer.failed", "consumer", CONSUMER_NAME);
        this.duplicateCounter = meterRegistry.counter("payment_mock.consumer.duplicate", "consumer", CONSUMER_NAME);
        this.paymentSuccessCounter = meterRegistry.counter("payment_mock.result.total", "result", "success");
        this.paymentFailureCounter = meterRegistry.counter("payment_mock.result.total", "result", "failed");
    }

    @Transactional
    @RabbitListener(queues = RabbitPaymentConfig.Q_MAIN, containerFactory = "paymentRabbitListenerContainerFactory")
    public void handle(EventEnvelope<PaymentRequestedPayload> event) {
        UUID eventId = event.eventId();
        if (processedEventsRepository.existsById(eventId)) {
            log.info("Duplicate payment.requested eventId={}, skipping", eventId);
            duplicateCounter.increment();
            return;
        }

        try {
            simulatePaymentDelay();

            boolean success = random.nextBoolean();
            UUID paymentId = UUID.randomUUID();

            if (success) {
                PaymentSucceededPayload payload = new PaymentSucceededPayload(
                        event.payload().orderId(),
                        paymentId,
                        Instant.now()
                );
                EventEnvelope<PaymentSucceededPayload> envelope = EventEnvelope.of(
                        EventTypes.PAYMENT_SUCCEEDED,
                        event.traceId(),
                        payload
                );
                rabbitTemplate.convertAndSend(RabbitPaymentConfig.EXCHANGE, "payments.succeeded", envelope);
                paymentSuccessCounter.increment();
                log.info("Payment succeeded for orderId={}, eventId={}", payload.orderId(), eventId);
            } else {
                PaymentFailedPayload payload = new PaymentFailedPayload(
                        event.payload().orderId(),
                        paymentId,
                        "Mock payment failure",
                        Instant.now()
                );
                EventEnvelope<PaymentFailedPayload> envelope = EventEnvelope.of(
                        EventTypes.PAYMENT_FAILED,
                        event.traceId(),
                        payload
                );
                rabbitTemplate.convertAndSend(RabbitPaymentConfig.EXCHANGE, "payments.failed", envelope);
                paymentFailureCounter.increment();
                log.info("Payment failed for orderId={}, eventId={}", payload.orderId(), eventId);
            }

            ProcessedEvents processed = new ProcessedEvents();
            processed.setEventId(eventId);
            processed.setProcessedAt(Instant.now());
            processed.setConsumerName(CONSUMER_NAME);
            processedEventsRepository.save(processed);

            processedCounter.increment();
        } catch (RuntimeException ex) {
            failedCounter.increment();
            log.error("Error processing payment.requested eventId={}", eventId, ex);
            throw ex; // приведет к retry/DLQ по конфигурации очередей
        }
    }

    private void simulatePaymentDelay() {
        try {
            Thread.sleep(500L + random.nextInt(500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

