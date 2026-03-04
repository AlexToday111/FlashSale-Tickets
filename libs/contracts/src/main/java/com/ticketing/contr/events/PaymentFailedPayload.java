package com.ticketing.contr.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailedPayload (
        @JsonProperty("orderId") UUID orderId,
        @JsonProperty("paymentId") UUID paymentId,
        @JsonProperty("reason") String reason,
        @JsonProperty("processedAt") Instant processedAt
){}
