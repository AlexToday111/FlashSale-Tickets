package com.ticketing.inventory.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public class ReservationRequestDTO {
    @NotNull(message = "User ID cannot be null")
    private UUID userId;

    @NotNull(message = "Event ID cannot be null")
    private UUID eventId;

    @NotEmpty(message = "Seats list cannot be empty")
    private Set<UUID> seats;

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
}
