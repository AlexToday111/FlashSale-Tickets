package com.ticketing.inventory.api.controller;

import com.ticketing.inventory.api.dto.ReservationDetailsDTO;
import com.ticketing.inventory.api.dto.ReservationRequestDTO;
import com.ticketing.inventory.api.dto.ReservationResponseDTO;
import com.ticketing.inventory.api.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createReservation(@Valid @RequestBody ReservationRequestDTO reservationRequest) {
        ReservationResponseDTO reservationResponse = reservationService.createReservation(reservationRequest);
        return new ResponseEntity<>(reservationResponse, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable UUID id) {
        reservationService.cancelReservation(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDetailsDTO> getReservationDetails(@PathVariable UUID id) {
        ReservationDetailsDTO reservationDetails = reservationService.getReservationDetails(id);
        return new ResponseEntity<>(reservationDetails, HttpStatus.OK);
    }

}
