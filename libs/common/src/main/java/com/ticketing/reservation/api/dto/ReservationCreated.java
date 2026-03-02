package com.ticketing.reservation.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationCreated(
        UUID eventId,
        UUID reservationId,
        UUID userId,
        List<UUID> seatIds,
        Instant createdAt,
        String eventType,
        int eventVersion,
        UUID correlationId
) {
}
