package com.ticketing.notification.service;

import com.ticketing.reservation.api.dto.ReservationCancelled;
import com.ticketing.reservation.api.dto.ReservationCreated;
import com.ticketing.reservation.api.dto.ReservationExpired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void handleReservationCreated(ReservationCreated event) {
        log.info("sent email: ReservationCreated reservationId={} userId={}", event.reservationId(), event.userId());
        simulateRandomFailure(event.reservationId().toString());
    }

    public void handleReservationCancelled(ReservationCancelled event) {
        log.info("sent email: ReservationCancelled reservationId={}", event.reservationId());
        simulateRandomFailure(event.reservationId().toString());
    }

    public void handleReservationExpired(ReservationExpired event) {
        log.info("sent email: ReservationExpired reservationId={}", event.reservationId());
        simulateRandomFailure(event.reservationId().toString());
    }

    private void simulateRandomFailure(String reservationId) {
        // simple deterministic failure: if last char is 'f' or '5' throw exception
        if (!reservationId.isEmpty()) {
            char last = reservationId.charAt(reservationId.length() - 1);
            if (last == 'f' || last == '5') {
                throw new IllegalStateException("Simulated failure for retry/DLQ testing");
            }
        }
    }
}
