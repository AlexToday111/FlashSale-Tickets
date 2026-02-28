package com.ticketing.inventory.api.service;

import com.ticketing.inventory.api.dto.ReservationDetailsDTO;
import com.ticketing.inventory.api.dto.ReservationRequestDTO;
import com.ticketing.inventory.api.dto.ReservationResponseDTO;
import com.ticketing.inventory.api.model.Reservation;
import com.ticketing.inventory.api.repository.ReservationRepository;
import com.ticketing.inventory.api.repository.SeatStateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatStateRepository seatStateRepository;

    public ReservationService(ReservationRepository reservationRepository, SeatStateRepository seatStateRepository) {
        this.reservationRepository = reservationRepository;
        this.seatStateRepository = seatStateRepository;
    }

    public ReservationResponseDTO createReservation(ReservationRequestDTO reservationRequest){
        Reservation reservation = new Reservation();
        reservation.setUserId(reservationRequest.getUserId());
        reservation.setEventId(reservationRequest.getEventId());
        reservation.setEventId(reservationRequest.getEventId());
        reservation.setSeats(reservationRequest.getSeats());
        reservation.setStatus(Reservation.Status.HELD);
        reservation.setExpiresAt(LocalDateTime.now().plusHours(1));

        reservationRepository.save(reservation);

        ReservationResponseDTO response = new ReservationResponseDTO();
        response.setReservationId(reservation.getReservationId());
        response.setStatus(reservation.getStatus().toString());
        response.setExpiresAt(reservation.getExpiresAt());
        response.setSeats(reservationRequest.getSeats());

        return response;
    }

    public void cancelReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findByReservationId(reservationId);

        if (reservation != null && reservation.getStatus() != Reservation.Status.CANCELLED){
            reservation.setStatus(Reservation.Status.CANCELLED);
            reservationRepository.save(reservation);
        }
    }
    public ReservationDetailsDTO getReservationDetails(UUID reservationId) {
        Reservation reservation = reservationRepository.findByReservationId();

        if (reservation != null) {
            ReservationDetailsDTO dto = new ReservationDetailsDTO();
            dto.setReservationId(reservation.getReservationId());
            dto.setStatus(reservation.getStatus().toString());
            dto.setSeats(reservation.getSeats());
            dto.setExpiresAt(reservation.getExpiresAt());
            dto.setUserId(reservation.getUserId());
            dto.setEventId(reservation.getEventId());

            return dto;
        }
        return null;
    }
}
