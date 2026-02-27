package com.ticketing.catalog.api.service;

import com.ticketing.catalog.api.dto.*;
import com.ticketing.catalog.api.model.EventEntity;
import com.ticketing.catalog.api.model.SeatEntity;
import com.ticketing.catalog.api.repository.EventRepository;
import com.ticketing.catalog.api.repository.SeatRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class EventService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    public EventService(EventRepository eventRepository, SeatRepository seatRepository) {
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public CreateEventResponse createEvent(CreateEventRequest request) {
        if (request.seats() == null || request.seats().isEmpty()) {
            throw new BadRequestException("seats must not be empty");
        }

        ensureNoDuplicateSeats(request.seats());

        EventEntity event = new EventEntity();
        event.setTitle(request.title());
        event.setVenue(request.venue());
        event.setStartsAt(request.startsAt());
        event.setStatus(EventEntity.Status.PUBLISHED);

        if (event.getCreatedAt() == null) {
            Instant now = Instant.now();
            event.setCreatedAt(now);
            event.setUpdatedAt(now);
        }

        EventEntity savedEvent = eventRepository.save(event);

        List<SeatEntity> seats = request.seats().stream()
                .map(s -> {
                    SeatEntity seat = new SeatEntity();
                    seat.setEvent(savedEvent);
                    seat.setSection(s.section());
                    seat.setRowLabel(s.row());
                    seat.getSeatNumber();
                    seat.setPriceCents(s.price());
                    return seat;
                })
                .toList();
        seatRepository.saveAll(seats);
        return new CreateEventResponse(savedEvent.getId(), savedEvent.getCreatedAt());
    }

    public GetEventsResponse getEvents(int limit, int offset){
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(0, offset);

        int page = safeOffset / safeLimit;

        var pageReq = PageRequest.of(page, safeLimit, Sort.by(Sort.Direction.DESC, "startsAt"));
        var pageResult = eventRepository.findAll(pageReq);

        List<EventSummaryDto> items = pageResult.getContent().stream()
                .map(e -> new EventSummaryDto(
                        e.getId(),
                        e.getTitle(),
                        e.getVenue(),
                        e.getStartsAt(),
                        EventStatusDto.valueOf(e.getStatus().name())
                ))
                .toList();
        return new GetEventsResponse(items, safeLimit, safeOffset, pageResult.getTotalElements());
    }

    public GetEventSeatsResponse getEventSeats(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("event not found: " + eventId);
        }

        List<SeatEntity> seats = seatRepository
                .findByEvent_IdOrderBySectionAscRowLabelAscSeatNumberAsc(eventId);

        List<SeatDto> seatDtos = seats.stream()
                .map(s -> new SeatDto(
                        s.getId(),
                        s.getSection(),
                        s.getRowLabel(),
                        s.getSeatNumber(),
                        s.getPriceCents()
                ))
                .toList();

        return new GetEventSeatsResponse(eventId, seatDtos);
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) return DEFAULT_LIMIT;
        return Math.min(limit, MAX_LIMIT);
    }

    private void ensureNoDuplicateSeats(List<CreateSeatRequest> seats) {
        Set<String> seen = new HashSet<>();
        List<String> duplicates = new ArrayList<>();

        for (CreateSeatRequest s : seats) {
            String key = (s.section().trim() + "|" + s.row().trim() + "|" + s.number().trim()).toLowerCase();
            if (!seen.add(key)) {
                duplicates.add(key);
            }
        }

        if (!duplicates.isEmpty()) {
            throw new BadRequestException("duplicate seats in request: " + duplicates);
        }
    }
}
