package com.ticketing.order.Api.service;

import com.ticketing.order.Api.dto.CreateOrderRequest;
import com.ticketing.order.Api.dto.OrderResponse;
import com.ticketing.order.Api.dto.PaymentRequestEvent;
import com.ticketing.order.Api.dto.ReservationView;
import com.ticketing.order.Api.Mapper.OrderMapper;
import com.ticketing.order.Api.model.OrderEntity;
import com.ticketing.order.Api.repository.OrderRepository;
import com.ticketing.order.infra.client.InventoryClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final OrderMapper orderMapper;
    private final RabbitTemplate rabbitTemplate;

    public OrderService(OrderRepository orderRepository,
                        InventoryClient inventoryClient,
                        OrderMapper orderMapper,
                        RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.orderMapper = orderMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        UUID reservationId = request.reservationId();
        if (reservationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reservationId is required");
        }

        if (orderRepository.existsByReservationId(reservationId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order already exists for reservation " + reservationId);
        }

        ReservationView reservation = inventoryClient.getReservation(reservationId);
        if (!"HELD".equalsIgnoreCase(reservation.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation is not in HELD status");
        }

        OrderEntity order = new OrderEntity();
        order.setUserId(reservation.userId());
        order.setReservationId(reservation.reservationId());
        order.setStatus(OrderEntity.Status.PENDING_PAYMENT);
        order.setTotal(reservation.total());
        order.setCreatedAt(Instant.now());

        OrderEntity saved = orderRepository.save(order);

        publishPaymentRequested(saved, reservation);

        return orderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderId));
        return orderMapper.toResponse(order);
    }

    private void publishPaymentRequested(OrderEntity order, ReservationView reservation) {
        PaymentRequestEvent event = new PaymentRequestEvent(
                null,
                Instant.now(),
                order.getOrderId(),
                order.getReservationId(),
                order.getUserId(),
                order.getTotal()
        );
        rabbitTemplate.convertAndSend("payments.exchange", "payments.requested", event);
    }
}

