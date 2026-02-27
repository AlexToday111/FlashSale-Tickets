package com.ticketing.catalog.api.controller;

import com.ticketing.catalog.api.dto.CreateEventRequest;
import com.ticketing.catalog.api.dto.CreateEventResponse;
import com.ticketing.catalog.api.dto.GetEventSeatsResponse;
import com.ticketing.catalog.api.dto.GetEventsResponse;
import com.ticketing.catalog.api.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateEventResponse createEvent(
            @Valid @RequestBody CreateEventRequest request
    ) {
        return eventService.createEvent(request);
    }

    @GetMapping
    public GetEventsResponse getEvents(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ){
        if (limit == 20 && offset == 0){
            return eventService.getEventFirstPage();
        } else {
            return eventService.getEvents(limit, offset);
        }
    }

    @GetMapping("/{id}/seats")
    public GetEventSeatsResponse getEventSeats(@PathVariable UUID id){
        return eventService.getEventSeats(id);
    }
}
