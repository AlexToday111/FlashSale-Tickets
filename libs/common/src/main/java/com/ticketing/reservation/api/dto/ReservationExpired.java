package com.ticketing.reservation.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ReservationExpired(
        UUID reservationId,
        Instant expiredAt,
        String eventType,
        int eventVersion,
        UUID correlationId
) {
}
