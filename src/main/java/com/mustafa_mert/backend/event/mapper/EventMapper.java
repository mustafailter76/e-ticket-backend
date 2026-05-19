package com.mustafa_mert.backend.event.mapper;

import com.mustafa_mert.backend.event.dto.CreateEventRequest;
import com.mustafa_mert.backend.event.dto.EventResponse;
import com.mustafa_mert.backend.event.dto.EventSalesResponse;
import com.mustafa_mert.backend.event.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public Event createEventRequestToEvent(CreateEventRequest createEventRequest) {
        return Event.builder()
                .name(createEventRequest.getName())
                .category(createEventRequest.getCategory())
                .location(createEventRequest.getLocation())
                .dateTime(createEventRequest.getDateTime())
                .imageUrl(createEventRequest.getImageUrl())
                .description(createEventRequest.getDescription())
                .price(createEventRequest.getPrice())
                .totalStock(createEventRequest.getTotalStock())
                .availableStock(createEventRequest.getTotalStock())
                .build();
    }

    public EventResponse eventToEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .category(event.getCategory())
                .location(event.getLocation())
                .dateTime(event.getDateTime())
                .imageUrl(event.getImageUrl())
                .description(event.getDescription())
                .price(event.getPrice())
                .totalStock(event.getTotalStock())
                .availableStock(event.getAvailableStock())
                .build();
    }

    public EventSalesResponse eventToEventSalesResponse(Event event) {
        return EventSalesResponse.builder()
                .eventId(event.getId())
                .eventName(event.getName())
                .category(event.getCategory())
                .totalStock(event.getTotalStock())
                .availableStock(event.getAvailableStock())
                .imageUrl(event.getImageUrl())
                .build();
    }
}
