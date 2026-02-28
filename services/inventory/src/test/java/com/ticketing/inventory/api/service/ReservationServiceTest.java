package com.ticketing.inventory.api.service;

import com.ticketing.inventory.api.dto.ReservationRequestDTO;
import com.ticketing.inventory.api.dto.ReservationResponseDTO;
import com.ticketing.inventory.api.model.Reservation;
import com.ticketing.inventory.api.model.Seat;
import com.ticketing.inventory.api.repository.ReservationRepository;
import com.ticketing.inventory.api.repository.SeatStateRepository;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import java.util.*;

import static org.hamcrest.Matchers.any;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SeatStateRepository SeatStateRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateReservation() {
        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setUserId(UUID.randomUUID());
        request.setEventId(UUID.randomUUID());
        request.setSeats(Arrays.asList(UUID.randomUUID()));

        when(SeatStateRepository.findAllById(anyList())).thenReturn(new Set<Seat>());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(new Reservation());

        ReservationResponseDTO response = reservationService.createReservation(request);

        assertNotNull(response);
        assertEquals("HELD", response.getStatus());
    }

    @Test
    public void testCancelReservation() {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.HELD);

        when(reservationRepository.findByReservationId(reservationId)).thenReturn(reservation);

        reservationService.cancelReservation(reservationId);

        verify(reservationRepository).save(reservation);
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }

    @Test
    public void testGetReservationDetails() {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = new Reservation();
        reservation.setReservationId(reservationId);
        reservation.setStatus(ReservationStatus.HELD);

        when(reservationRepository.findByReservationId(reservationId)).thenReturn(reservation);

        ReservationDetailsDTO response = reservationService.getReservationDetails(reservationId);

        assertNotNull(response);
        assertEquals(reservationId, response.getReservationId());
        assertEquals("HELD", response.getStatus());
    }
}
