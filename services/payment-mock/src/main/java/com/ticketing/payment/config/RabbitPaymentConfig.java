package com.ticketing.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitPaymentConfig {

    public static final String EXCHANGE = "payments.exchange";
    public static final String Q_MAIN = "payments.requested.queue";
    public static final String Q_RETRY = "payments.requested.retry.queue";
    public static final String Q_DLQ = "payments.requested.dlq.queue";

    public static final String RK_REQUESTED = "payments.requested";

    public static final int RETRY_TTL_MS = 5_000;

    @Bean
    public TopicExchange paymentsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentsRequestedQueue() {
        return QueueBuilder.durable(Q_MAIN)
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", Q_RETRY)
                .build();
    }

    @Bean
    public Queue paymentsRequestedRetryQueue() {
        return QueueBuilder.durable(Q_RETRY)
                .withArgument("x-message-ttl", RETRY_TTL_MS)
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RK_REQUESTED)
                .build();
    }

    @Bean
    public Queue paymentsRequestedDlqQueue() {
        return QueueBuilder.durable(Q_DLQ).build();
    }

    @Bean
    public Binding paymentsRequestedBinding(Queue paymentsRequestedQueue, TopicExchange paymentsExchange) {
        return BindingBuilder.bind(paymentsRequestedQueue).to(paymentsExchange).with(RK_REQUESTED);
    }

    @Bean
    public Binding paymentsRequestedRetryBinding(Queue paymentsRequestedRetryQueue, TopicExchange paymentsExchange) {
        return BindingBuilder.bind(paymentsRequestedRetryQueue).to(paymentsExchange).with(Q_RETRY);
    }

    @Bean
    public Binding paymentsRequestedDlqBinding(Queue paymentsRequestedDlqQueue, TopicExchange paymentsExchange) {
        return BindingBuilder.bind(paymentsRequestedDlqQueue).to(paymentsExchange).with(Q_DLQ);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory paymentRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDefaultRequeueRejected(false);
        factory.setErrorHandler(new ConditionalRejectingErrorHandler());
        return factory;
    }
}

