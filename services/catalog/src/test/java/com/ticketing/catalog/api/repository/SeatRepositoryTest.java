package com.ticketing.catalog.api.repository;

import com.ticketing.catalog.api.model.EventEntity;
import com.ticketing.catalog.api.model.SeatEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class SeatRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("catalog")
            .withUsername("catalog")
            .withPassword("catalog");

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Test
    void findByEventIdReturnsSeatsOrderedBySectionRowAndSeatNumber() {
        // arrange: create and persist event
        EventEntity event = new EventEntity();
        event.setTitle("Test Event");
        event.setVenue("Main Hall");
        event.setStartsAt(Instant.parse("2030-01-01T10:00:00Z"));
        EventEntity savedEvent = eventRepository.save(event);

        // arrange: create a few seats in non-sorted order
        SeatEntity seatB2 = new SeatEntity();
        seatB2.setEvent(savedEvent);
        seatB2.setSection("B");
        seatB2.setRowLabel("2");
        seatB2.setSeatNumber("10");
        seatB2.setPriceCents(2000);

        SeatEntity seatA3 = new SeatEntity();
        seatA3.setEvent(savedEvent);
        seatA3.setSection("A");
        seatA3.setRowLabel("3");
        seatA3.setSeatNumber("5");
        seatA3.setPriceCents(1500);

        SeatEntity seatA1 = new SeatEntity();
        seatA1.setEvent(savedEvent);
        seatA1.setSection("A");
        seatA1.setRowLabel("1");
        seatA1.setSeatNumber("1");
        seatA1.setPriceCents(1000);

        seatRepository.saveAll(List.of(seatB2, seatA3, seatA1));

        // act
        List<SeatEntity> seats = seatRepository
                .findByEvent_IdOrderBySectionAscRowLabelAscSeatNumberAsc(savedEvent.getId());

        // assert
        assertThat(seats).hasSize(3);
        assertThat(seats)
                .extracting(SeatEntity::getSection, SeatEntity::getRowLabel, SeatEntity::getSeatNumber)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("A", "1", "1"),
                        org.assertj.core.groups.Tuple.tuple("A", "3", "5"),
                        org.assertj.core.groups.Tuple.tuple("B", "2", "10")
                );
    }
}

