package com.ticketing.catalog.api.dto;

import java.time.Instant;
import java.util.UUID;

public record CreateEventResponse (
        UUID id,
        Instant createdAt){
}
