package com.mustafa_mert.backend.event.controller;

import com.mustafa_mert.backend.common.response.RootEntity;
import com.mustafa_mert.backend.event.dto.CreateEventRequest;
import com.mustafa_mert.backend.event.dto.EventResponse;
import com.mustafa_mert.backend.event.dto.SalesDashboardResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface EventController {

    ResponseEntity<RootEntity<?>> deleteEvent(Long eventId);

    ResponseEntity<RootEntity<EventResponse>> createEvent(CreateEventRequest createEventRequest);

    ResponseEntity<RootEntity<SalesDashboardResponse>> getSalesDashboard();

    ResponseEntity<RootEntity<List<EventResponse>>> getEvents(String name, String category);
}
