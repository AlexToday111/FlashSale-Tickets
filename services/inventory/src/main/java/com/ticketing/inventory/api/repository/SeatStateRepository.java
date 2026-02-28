package com.ticketing.inventory.api.repository;

import com.ticketing.inventory.api.model.SeatState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatStateRepository extends JpaRepository<SeatState, UUID> {
    SeatState findBySeatId(UUID seatId);

    List<SeatState> findByEventId(UUID eventId);

    boolean existsBySeatAndVersion(UUID seatId, int version);
}
