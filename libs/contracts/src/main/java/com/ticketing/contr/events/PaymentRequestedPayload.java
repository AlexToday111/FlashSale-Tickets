package com.ticketing.contr.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record PaymentRequestedPayload (
        @JsonProperty("orderId") UUID orderId,
        @JsonProperty("userId") UUID userId,
        @JsonProperty("amount") long amount,
        @JsonProperty("currency") String currency,
        @JsonProperty("paymentAttempt") int paymentAttempt
){}
