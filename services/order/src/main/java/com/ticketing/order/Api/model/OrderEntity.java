package com.ticketing.order.Api.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders",
    indexes = {
        @Index(name = "idx_orders_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_orders_status_created", columnList = "status, created_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_orders_reservation", columnNames = "reservation_id")
    }
)
public class OrderEntity {
    @Id
    @GeneratedValue
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private int total;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public enum Status{
        PENDING_PAYMENT,
        PAID,
        CANCELLED
    }
}
