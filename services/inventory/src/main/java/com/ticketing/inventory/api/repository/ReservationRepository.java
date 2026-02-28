package com.ticketing.inventory.api.repository;

import com.ticketing.inventory.api.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Reservation findByReservationId(UUID reservationId);

    List<Reservation> findByUserId(UUID userId);

    List<Reservation> findByEventId(UUID eventId);

    List<Reservation> findByExpiresAtBefore(Instant now);
}
