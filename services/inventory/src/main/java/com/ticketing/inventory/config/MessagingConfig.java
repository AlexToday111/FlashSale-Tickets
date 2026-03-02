package com.ticketing.inventory.config;

import com.ticketing.reservation.api.ReservationEventBindings;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Bean
    public TopicExchange reservationEventsExchange() {
        return new TopicExchange(ReservationEventBindings.EXCHANGE, true, false);
    }
}
