package com.ticketing.order.Api.repository;

import com.ticketing.order.Api.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    Optional<OrderEntity> findByReservationId(UUID reservationId);
    boolean existsByReservationId(UUID reservationId);
    long countByStatus(OrderEntity.Status status);
}
