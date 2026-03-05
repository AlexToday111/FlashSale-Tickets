package com.ticketing.order.infra.metrics;

import com.ticketing.order.Api.model.OrderEntity;
import com.ticketing.order.Api.repository.OrderRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OrderBusinessMetrics {

    private final OrderRepository orderRepository;

    public OrderBusinessMetrics(OrderRepository orderRepository,
                                MeterRegistry meterRegistry) {
        this.orderRepository = orderRepository;

        Gauge.builder("business_sold_count", this, OrderBusinessMetrics::soldCount)
                .description("Number of paid orders")
                .register(meterRegistry);
    }

    private double soldCount() {
        return orderRepository.countByStatus(OrderEntity.Status.PAID);
    }
}
