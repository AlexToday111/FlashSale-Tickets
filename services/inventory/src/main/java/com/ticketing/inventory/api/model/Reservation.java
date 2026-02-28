package com.ticketing.inventory.api.model;


import jakarta.persistence.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "reservation", indexes = {
        @Index(name = "idx_reservation_user", columnList = "user_id"),
        @Index(name = "idx_reservation_event", columnList = "event_id"),
        @Index(name = "idx_reservation_status", columnList = "status")
})
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID reservationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @ElementCollection
    @CollectionTable(name = "reservation_seats",
            joinColumns = @JoinColumn(name = "reservation_id")
    )
    @Column(name = "seat_id")
    private Set<UUID> seats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public enum Status{
        HELD,
        CANCELLED,
        EXPIRED
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Set<UUID> getSeats() {
        return seats;
    }

    public void setSeats(Set<UUID> seats) {
        this.seats = seats;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
