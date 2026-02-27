package com.ticketing.catalog.api.repository;

import com.ticketing.catalog.api.model.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {
}