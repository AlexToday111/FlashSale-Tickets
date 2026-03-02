package com.ticketing.inventory.api.scheduler;

import com.ticketing.inventory.api.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReservationExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpirationScheduler.class);

    private final ReservationService reservationService;

    public ReservationExpirationScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkExpiredReservations() {
        int expired = reservationService.expireReservations();
        if (expired > 0) {
            log.info("Expired reservations count: {}", expired);
        }
    }
}
