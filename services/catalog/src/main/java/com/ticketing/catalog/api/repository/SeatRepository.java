package com.ticketing.catalog.api.repository;

import com.ticketing.catalog.api.model.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<SeatEntity, UUID> {
    List<SeatEntity> findByEvent_IdOrderBySectionAscRowLabelAscSeatNumberAsc(UUID eventId);
}
