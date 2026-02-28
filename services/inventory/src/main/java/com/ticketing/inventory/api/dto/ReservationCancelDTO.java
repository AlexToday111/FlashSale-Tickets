package com.ticketing.inventory.api.dto;

import java.util.UUID;

public class ReservationCancelDTO {
    private UUID reservationId;

    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }
}
