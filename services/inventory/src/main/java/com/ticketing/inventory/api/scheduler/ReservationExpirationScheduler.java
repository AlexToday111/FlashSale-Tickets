package com.ticketing.inventory.api.scheduler;

import com.ticketing.inventory.api.model.Reservation;
import com.ticketing.inventory.api.model.SeatState;
import com.ticketing.inventory.api.repository.ReservationRepository;
import com.ticketing.inventory.api.repository.SeatStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class ReservationExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpirationScheduler.class);

    private final ReservationRepository reservationRepository;
    private final SeatStateRepository seatStateRepository;

    @Autowired
    public ReservationExpirationScheduler(ReservationRepository reservationRepository,
                                          SeatStateRepository seatStateRepository) {
        this.reservationRepository = reservationRepository;
        this.seatStateRepository = seatStateRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkExpiredReservations() {
        Instant now = Instant.now();
        List<Reservation> expiredReservations = reservationRepository.findByExpiresAtBefore(now);

        if (!expiredReservations.isEmpty()) {
            log.info("Found {} expired reservations", expiredReservations.size());
        }

        for (Reservation reservation : expiredReservations) {
            if (reservation.getStatus() == Reservation.Status.HELD) {
                reservation.setStatus(Reservation.Status.EXPIRED);
                reservationRepository.save(reservation);

                if (reservation.getSeats() != null) {
                    for (UUID seatId : reservation.getSeats()) {
                        SeatState seatState = seatStateRepository.findBySeatId(seatId);
                        if (seatState != null && seatState.getStatus() == SeatState.Status.BOOKED) {
                            seatState.setStatus(SeatState.Status.AVAILABLE);
                            seatState.setUpdatedAt(Instant.now());
                            seatStateRepository.save(seatState);
                            log.debug("Released seat {} from expired reservation {}", seatId, reservation.getReservationId());
                        }
                    }
                    log.info("Expired reservation {} and released {} seats", 
                        reservation.getReservationId(), reservation.getSeats().size());
                }
            }
        }
    }
}
