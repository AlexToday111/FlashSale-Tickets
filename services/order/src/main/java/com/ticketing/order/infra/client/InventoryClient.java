package com.ticketing.order.infra.client;

import com.ticketing.order.Api.dto.ReservationView;
import com.ticketing.order.Api.exception.ReservationNotFoundException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
public class InventoryClient {
    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private final WebClient webClient;

    public InventoryClient(@Value("${inventory.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter((request, next) -> {
                    String requestId = MDC.get("requestId");
                    if (requestId == null || requestId.isBlank()) {
                        return next.exchange(request);
                    }
                    return next.exchange(
                            org.springframework.web.reactive.function.client.ClientRequest.from(request)
                                    .header(CORRELATION_HEADER, requestId)
                                    .build()
                    );
                })
                .build();
    }

    public ReservationView getReservation(UUID reservationId) {
        return webClient.get()
                .uri("/reservation/{id}", reservationId)
                .retrieve()
                .onStatus(s -> s == HttpStatus.NOT_FOUND,
                        resp -> Mono.error(new ReservationNotFoundException("Reservation not found: " + reservationId)))
                .bodyToMono(ReservationView.class)
                .timeout(Duration.ofSeconds(2))
                .block();
    }
}
