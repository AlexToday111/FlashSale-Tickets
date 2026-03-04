package com.ticketing.payment.model;

import com.fasterxml.jackson.annotation.JsonTypeId;
import jakarta.persistence.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
public class ProcessedEvents {
    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "consumer_name")
    private String consumerName;

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }
}
