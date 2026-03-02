package com.ticketing.notification.config.messaging;

import com.ticketing.reservation.api.ReservationEventBindings;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String Q_MAIN = ReservationEventBindings.Q_NOTIFICATION_MAIN;
    public static final String Q_RETRY = ReservationEventBindings.Q_NOTIFICATION_RETRY;
    public static final String Q_DLQ = ReservationEventBindings.Q_NOTIFICATION_DLQ;

    public static final int RETRY_TTL_MS = 10_000;
    public static final int MAX_RETRIES = 5;

    @Bean
    public TopicExchange reservationEventsExchange(){
        return new TopicExchange(ReservationEventBindings.EXCHANGE, true, false);
    }

    @Bean
    public Queue mainQueue() {
        return QueueBuilder.durable(Q_MAIN)
                .withArgument("x-dead-letter-exchange", ReservationEventBindings.EXCHANGE)
                .withArgument("x-dead-letter-routing-key", Q_RETRY)
                .build();
    }

    @Bean
    public Queue retryQueue(){
        return QueueBuilder.durable(Q_RETRY)
                .withArgument("x-message-ttl", RETRY_TTL_MS)
                .withArgument("x-dead-letter-exchange", ReservationEventBindings.EXCHANGE)
                .withArgument("x-dead-letter-routing-key", Q_MAIN)
                .build();
    }

    @Bean
    public Queue dlqQueue(){
        return QueueBuilder.durable(Q_DLQ).build();
    }
    @Bean
    public Binding bindCreated(Queue mainQueue, TopicExchange reservationEventsExchange) {
        return BindingBuilder.bind(mainQueue).to(reservationEventsExchange).with(ReservationEventBindings.RK_CREATED);
    }

    @Bean
    public Binding bindCancelled(Queue mainQueue, TopicExchange reservationEventsExchange) {
        return BindingBuilder.bind(mainQueue).to(reservationEventsExchange).with(ReservationEventBindings.RK_CANCELLED);
    }

    @Bean
    public Binding bindExpired(Queue mainQueue, TopicExchange reservationEventsExchange) {
        return BindingBuilder.bind(mainQueue).to(reservationEventsExchange).with(ReservationEventBindings.RK_EXPIRED);
    }

    @Bean
    public Binding bindRetryRoutingKey(Queue retryQueue, TopicExchange reservationEventsExchange) {
        return BindingBuilder.bind(retryQueue).to(reservationEventsExchange).with(ReservationEventBindings.Q_NOTIFICATION_RETRY);
    }

    @Bean
    public Binding bindDlqRoutingKey(Queue dlqQueue, TopicExchange reservationEventsExchange) {
        return BindingBuilder.bind(dlqQueue).to(reservationEventsExchange).with(ReservationEventBindings.Q_NOTIFICATION_DLQ);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDefaultRequeueRejected(false);
        factory.setErrorHandler(new ConditionalRejectingErrorHandler());
        return factory;
    }
}
