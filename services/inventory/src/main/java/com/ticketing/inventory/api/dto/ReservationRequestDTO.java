package com.ticketing.inventory.api.dto;

import java.util.Set;
import java.util.UUID;

public class ReservationRequestDTO {
    private UUID userId;
    private UUID eventId;
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
