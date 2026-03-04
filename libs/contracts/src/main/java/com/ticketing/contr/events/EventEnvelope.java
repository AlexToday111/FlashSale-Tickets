package com.ticketing.contr.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventEnvelope<T> (
        @JsonProperty("eventId") UUID eventId,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("occuredAt") Instant occuredAt,
        @JsonProperty("traceId") String traceId,
        @JsonProperty("payload") T payload
){
    public static <T> EventEnvelope<T> of(String eventType, String traceId, T payload){
        return new EventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                Instant.now(),
                traceId,
                payload
        );
    }
}
