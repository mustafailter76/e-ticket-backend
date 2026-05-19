package com.mustafa_mert.backend.event.controller;

import com.mustafa_mert.backend.common.response.RestBaseController;
import com.mustafa_mert.backend.common.response.RootEntity;
import com.mustafa_mert.backend.event.dto.CreateEventRequest;
import com.mustafa_mert.backend.event.dto.EventResponse;
import com.mustafa_mert.backend.event.dto.SalesDashboardResponse;
import com.mustafa_mert.backend.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
@RestController
public class EventControllerImpl extends RestBaseController implements EventController {

    private final EventService eventService;

    @DeleteMapping("/delete/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<RootEntity<?>> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.ok(RootEntity.ok());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<RootEntity<EventResponse>> createEvent(@Valid @RequestBody CreateEventRequest createEventRequest) {
        return ok(eventService.createEvent(createEventRequest));
    }

    @GetMapping("/sales-dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<RootEntity<SalesDashboardResponse>> getSalesDashboard() {
        return ok(eventService.getSalesDashboard());
    }

    @GetMapping
    @Override
    public ResponseEntity<RootEntity<List<EventResponse>>> getEvents(@RequestParam(required = false) String name,
                                                                     @RequestParam(required = false) String category) {
        return ok(eventService.getEvents(name, category));
    }
}
