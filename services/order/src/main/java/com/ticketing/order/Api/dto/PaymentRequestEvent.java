package com.ticketing.order.Api.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentRequestEvent (
        UUID eventId,
        Instant occuredAt,
        UUID orderId,
        UUID reservationId,
        UUID userId,
        int amount
){ }
