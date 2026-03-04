package com.ticketing.payment;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class PaymentMockApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentMockApplication.class, args);
    }
}

