package com.ticketing.order.Api.dto;

import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID userId,
        UUID reservationId,
        String status,
        int total,
        Instant createdAt
) {}

