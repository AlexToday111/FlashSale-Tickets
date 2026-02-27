package com.ticketing.catalog.api.dto;

import java.util.List;

public record GetEventsResponse (
        List<EventSummaryDto> items,
        int limit,
        int offset,
        long total
){}
