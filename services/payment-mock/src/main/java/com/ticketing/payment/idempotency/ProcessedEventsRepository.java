package com.ticketing.payment.idempotency;

import com.ticketing.payment.model.ProcessedEvents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventsRepository extends JpaRepository<ProcessedEvents, UUID> {
}

