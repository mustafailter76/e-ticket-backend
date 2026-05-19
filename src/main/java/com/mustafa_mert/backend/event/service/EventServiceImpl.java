package com.mustafa_mert.backend.event.service;

import com.mustafa_mert.backend.common.exception.BaseException;
import com.mustafa_mert.backend.common.exception.ErrorMessage;
import com.mustafa_mert.backend.common.exception.MessageType;
import com.mustafa_mert.backend.event.mapper.EventMapper;
import com.mustafa_mert.backend.event.dto.CreateEventRequest;
import com.mustafa_mert.backend.event.dto.EventResponse;
import com.mustafa_mert.backend.event.dto.EventSalesResponse;
import com.mustafa_mert.backend.event.dto.SalesDashboardResponse;
import com.mustafa_mert.backend.event.entity.Event;
import com.mustafa_mert.backend.event.repository.EventRepository;
import com.mustafa_mert.backend.security.CurrentUserProvider;
import com.mustafa_mert.backend.user.entity.User;
import com.mustafa_mert.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Transactional
    @Override
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.EVENT_NOT_FOUND)));
        if (!event.getTotalStock().equals(event.getAvailableStock())) {
            throw new BaseException(new ErrorMessage(MessageType.CANNOT_DELETE_EVENT_WITH_PURCHASES));
        }

        eventRepository.delete(event);
    }

    @Transactional
    @Override
    public EventResponse createEvent(CreateEventRequest createEventRequest) {
        Event event = eventMapper.createEventRequestToEvent(createEventRequest);
        Event savedEvent = eventRepository.save(event);

        return eventMapper.eventToEventResponse(savedEvent);
    }

    @Override
    public SalesDashboardResponse getSalesDashboard() {
        List<Event> events = eventRepository.findAll();
        List<EventSalesResponse> eventSalesResponses = new ArrayList<>();

        int totalSoldTickets = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Event event : events) {
            int soldTickets = event.getTotalStock() - event.getAvailableStock();

            BigDecimal eventRevenue = event.getPrice()
                    .multiply(BigDecimal.valueOf(soldTickets));

            EventSalesResponse eventSalesResponse = eventMapper.eventToEventSalesResponse(event);
            eventSalesResponse.setSoldTickets(soldTickets);
            eventSalesResponse.setTotalRevenue(eventRevenue);

            eventSalesResponses.add(eventSalesResponse);

            totalSoldTickets += soldTickets;
            totalRevenue = totalRevenue.add(eventRevenue);
        }

        SalesDashboardResponse response = new SalesDashboardResponse();
        response.setTotalEventCount(events.size());
        response.setTotalSoldTickets(totalSoldTickets);
        response.setTotalRevenue(totalRevenue);
        response.setEvents(eventSalesResponses);

        return response;
    }

    @Override
    public List<EventResponse> getEvents(String name, String category) {
        if (name != null && name.isBlank()) {
            name = null;
        }
        if (category != null && category.isBlank()) {
            category = null;
        }

        List<Event> events;
        if (name != null && category != null) {
            events = eventRepository.findByNameContainingIgnoreCaseAndCategoryIgnoreCaseOrderByDateTimeAsc(name, category);
        } else if (name != null) {
            events = eventRepository.findByNameContainingIgnoreCaseOrderByDateTimeAsc(name);
        } else if (category != null) {
            events = eventRepository.findByCategoryIgnoreCaseOrderByDateTimeAsc(category);
        } else {
            events = eventRepository.findAllByOrderByDateTimeAsc();
        }

        List<EventResponse> eventResponses = new ArrayList<>();
        for (Event event : events) {
            EventResponse eventResponse =  eventMapper.eventToEventResponse(event);
            eventResponses.add(eventResponse);
        }

        return eventResponses;
    }
}
