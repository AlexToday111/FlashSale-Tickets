package com.ticketing.order.Api.dto;

import java.util.UUID;

public record ReservationView (
        UUID reservationId,
        UUID userId,
        String status,
        int total
){}
