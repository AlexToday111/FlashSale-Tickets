package com.ticketing.catalog.api.dto;

import java.util.UUID;

public record SeatDto (
        UUID id,
        String section,
        String row,
        String number,
        long price
){}
