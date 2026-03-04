package com.ticketing.contr.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record PaymentSucceededPayload (
        @JsonProperty("orderId") UUID orderId,
        @JsonProperty("paymentId") UUID paymentId,
        @JsonProperty("processedAt") Instant processedAt
){ }
