package com.ticketing.catalog.api.dto;

import java.time.Instant;
import java.util.UUID;

public record EventSummaryDto (
        UUID id,
        String title,
        String venue,
        Instant startsAt,
        EventStatusDto status
){}
