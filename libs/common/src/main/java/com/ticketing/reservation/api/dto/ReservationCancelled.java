package com.ticketing.reservation.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ReservationCancelled(
        UUID reservationId,
        String reason,
        Instant cancelledAt,
        String eventType,
        int eventVersion,
        UUID correlationId
) {
}
