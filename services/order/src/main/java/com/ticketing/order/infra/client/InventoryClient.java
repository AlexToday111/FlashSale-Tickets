package com.ticketing.order.infra.client;

import com.ticketing.order.Api.dto.ReservationView;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class InventoryClient {
    private final WebClient webClient;

    public InventoryClient(@Value("${inventory.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public ReservationView getReservation(UUID reservationId) {
        return webClient.get()
                .uri("/reservations/{id}", reservationId)
                .retrieve()
                .onStatus(s -> s == HttpStatus.NOT_FOUND,
                        resp -> Mono.error(new NotFoundException("Reservation not found: " + reservationId)))
                .bodyToMono(ReservationView.class)
                .timeout(Duration.ofSeconds(2))
                .block();
    }
}
