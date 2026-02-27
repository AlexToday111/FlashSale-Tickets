package com.ticketing.catalog.api.dto;

import java.util.List;
import java.util.UUID;

public record GetEventSeatsResponse (
        UUID eventId,
        List<SeatDto> seats
){}
