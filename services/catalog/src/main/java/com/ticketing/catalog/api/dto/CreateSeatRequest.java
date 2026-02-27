package com.ticketing.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateSeatRequest (
        @NotBlank String section,
        @NotBlank String row,
        @NotBlank String number,
        @PositiveOrZero long price
){}
