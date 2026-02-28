package com.ticketing.inventory.api.service;

import com.ticketing.inventory.api.dto.ReservationDetailsDTO;
import com.ticketing.inventory.api.dto.ReservationRequestDTO;
import com.ticketing.inventory.api.dto.ReservationResponseDTO;
import com.ticketing.inventory.api.dto.SeatDTO;
import com.ticketing.inventory.api.exception.InvalidReservationException;
import com.ticketing.inventory.api.exception.ResourceNotFoundException;
import com.ticketing.inventory.api.model.Reservation;
import com.ticketing.inventory.api.model.SeatState;
import com.ticketing.inventory.api.repository.ReservationRepository;
import com.ticketing.inventory.api.repository.SeatStateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatStateRepository seatStateRepository;

    public ReservationService(ReservationRepository reservationRepository, SeatStateRepository seatStateRepository) {
        this.reservationRepository = reservationRepository;
        this.seatStateRepository = seatStateRepository;
    }

    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO reservationRequest){
        if (reservationRequest.getSeats() == null || reservationRequest.getSeats().isEmpty()) {
            throw new InvalidReservationException("Seats list cannot be empty");
        }

        Set<UUID> unavailableSeats = new HashSet<>();
        for (UUID seatId : reservationRequest.getSeats()) {
            SeatState seatState = seatStateRepository.findBySeatId(seatId);
            if (seatState == null) {
                throw new ResourceNotFoundException("Seat not found: " + seatId);
            }
            if (seatState.getStatus() != SeatState.Status.AVAILABLE) {
                unavailableSeats.add(seatId);
            }
        }

        if (!unavailableSeats.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Seats are not available: " + unavailableSeats);
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(reservationRequest.getUserId());
        reservation.setEventId(reservationRequest.getEventId());
        reservation.setSeats(reservationRequest.getSeats());
        reservation.setStatus(Reservation.Status.HELD);
        reservation.setExpiresAt(Instant.now().plusSeconds(3600));

        for (UUID seatId : reservationRequest.getSeats()) {
            SeatState seatState = seatStateRepository.findBySeatId(seatId);
            if (seatState != null) {
                seatState.setStatus(SeatState.Status.BOOKED);
                seatState.setUpdatedAt(Instant.now());
                seatStateRepository.save(seatState);
            }
        }

        reservationRepository.save(reservation);

        ReservationResponseDTO response = new ReservationResponseDTO();
        response.setReservationId(reservation.getReservationId());
        response.setStatus(reservation.getStatus().toString());
        response.setExpiresAt(LocalDateTime.ofInstant(reservation.getExpiresAt(), ZoneId.systemDefault()));
        
        Set<SeatDTO> seatDTOs = reservationRequest.getSeats().stream()
                .map(seatId -> {
                    SeatDTO seatDTO = new SeatDTO();
                    seatDTO.setSeatId(seatId);
                    return seatDTO;
                })
                .collect(Collectors.toSet());
        response.setSeats(seatDTOs);

        return response;
    }

    @Transactional
    public void cancelReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findByReservationId(reservationId);

        if (reservation == null) {
            throw new ResourceNotFoundException("Reservation not found: " + reservationId);
        }

        if (reservation.getStatus() == Reservation.Status.CANCELLED) {
            throw new InvalidReservationException("Reservation is already cancelled");
        }

        reservation.setStatus(Reservation.Status.CANCELLED);
        reservationRepository.save(reservation);

        if (reservation.getSeats() != null) {
            for (UUID seatId : reservation.getSeats()) {
                SeatState seatState = seatStateRepository.findBySeatId(seatId);
                if (seatState != null) {
                    seatState.setStatus(SeatState.Status.AVAILABLE);
                    seatState.setUpdatedAt(Instant.now());
                    seatStateRepository.save(seatState);
                }
            }
        }
    }
    public ReservationDetailsDTO getReservationDetails(UUID reservationId) {
        Reservation reservation = reservationRepository.findByReservationId(reservationId);

        if (reservation == null) {
            throw new ResourceNotFoundException("Reservation not found: " + reservationId);
        }

        ReservationDetailsDTO dto = new ReservationDetailsDTO();
        dto.setReservationId(reservation.getReservationId());
        dto.setStatus(reservation.getStatus().toString());
        dto.setExpiresAt(LocalDateTime.ofInstant(reservation.getExpiresAt(), ZoneId.systemDefault()));
        dto.setUserId(reservation.getUserId());
        dto.setEventId(reservation.getEventId());

        if (reservation.getSeats() != null) {
            Set<SeatDTO> seatDTOs = reservation.getSeats().stream()
                    .map(seatId -> {
                        SeatDTO seatDTO = new SeatDTO();
                        seatDTO.setSeatId(seatId);
                        return seatDTO;
                    })
                    .collect(Collectors.toSet());
            dto.setSeats(seatDTOs);
        }

        return dto;
    }
}
