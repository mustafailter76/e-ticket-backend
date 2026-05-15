package com.mustafa_mert.backend.event.service;

import com.mustafa_mert.backend.event.dto.CreateEventRequest;
import com.mustafa_mert.backend.event.dto.EventResponse;
import com.mustafa_mert.backend.event.dto.SalesDashboardResponse;

import java.util.List;

public interface EventService {

    void deleteEvent(Long eventId);

    EventResponse createEvent(CreateEventRequest createEventRequest);

    SalesDashboardResponse getSalesDashboard();

    List<EventResponse> getEvents(String name, String category);
}
