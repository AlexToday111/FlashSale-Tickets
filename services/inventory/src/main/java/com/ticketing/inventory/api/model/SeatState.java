package com.ticketing.inventory.api.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seat_state", indexes = {
        @Index(name = "idx_seat_status", columnList = "status"),
        @Index(name = "idx_seat_updated", columnList = "updated_at")
})
public class SeatState {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID seatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Version
    private Long version;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum Status{
        AVAILABLE,
        BOOKED,
        OCCUPIED
    }

    public UUID getSeatId() {
        return seatId;
    }

    public void setSeatId(UUID seatId) {
        this.seatId = seatId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
