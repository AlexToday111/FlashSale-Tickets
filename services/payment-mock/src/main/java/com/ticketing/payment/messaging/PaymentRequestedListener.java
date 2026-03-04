package com.ticketing.payment.messaging;

import com.ticketing.contr.events.EventEnvelope;
import com.ticketing.contr.events.PaymentRequestedPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestedListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentRequestedListener.class);

    @RabbitListener(queues = "payments.requested.queue")
    public void handle(EventEnvelope<PaymentRequestedPayload> event) {
        log.info("Received payment request event: {}", event);
        // Here we only mock processing; real implementation would call payment provider
    }
}

