package com.ticketing.inventory.api.metrics;

import com.ticketing.inventory.api.model.Reservation;
import com.ticketing.inventory.api.repository.ReservationRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class InventoryBusinessMetrics {

    private final ReservationRepository reservationRepository;

    public InventoryBusinessMetrics(ReservationRepository reservationRepository,
                                    MeterRegistry meterRegistry) {
        this.reservationRepository = reservationRepository;

        Gauge.builder("business_reserved_count", this, InventoryBusinessMetrics::reservedCount)
                .description("Number of active held reservations")
                .register(meterRegistry);
    }

    private double reservedCount() {
        return reservationRepository.countByStatus(Reservation.Status.HELD);
    }
}
