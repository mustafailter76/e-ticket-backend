package com.mustafa_mert.backend.event;

import com.mustafa_mert.backend.common.exception.BaseException;
import com.mustafa_mert.backend.event.dto.CreateEventRequest;
import com.mustafa_mert.backend.event.dto.EventResponse;
import com.mustafa_mert.backend.event.dto.EventSalesResponse;
import com.mustafa_mert.backend.event.dto.SalesDashboardResponse;
import com.mustafa_mert.backend.event.entity.Event;
import com.mustafa_mert.backend.event.mapper.EventMapper;
import com.mustafa_mert.backend.event.repository.EventRepository;
import com.mustafa_mert.backend.event.service.EventServiceImpl;
import com.mustafa_mert.backend.security.CurrentUserProvider;
import com.mustafa_mert.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event;
    private EventResponse eventResponse;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1L);
        event.setName("Concert");
        event.setCategory("Music");
        event.setDescription("Test description");
        event.setLocation("Istanbul");
        event.setPrice(BigDecimal.valueOf(100));
        event.setTotalStock(50);
        event.setAvailableStock(50);
        event.setDateTime(LocalDateTime.now().plusDays(5));

        eventResponse = EventResponse.builder()
                .id(1L)
                .name("Concert")
                .category("Music")
                .description("Test description")
                .location("Istanbul")
                .price(BigDecimal.valueOf(100))
                .totalStock(50)
                .availableStock(50)
                .dateTime(event.getDateTime())
                .build();
    }

    @Test
    void createEvent_shouldCreateEventAndReturnEventResponse() {
        CreateEventRequest request = new CreateEventRequest();
        request.setName("Concert");
        request.setCategory("Music");
        request.setDescription("Test description");
        request.setLocation("Istanbul");
        request.setPrice(BigDecimal.valueOf(100));
        request.setTotalStock(50);
        request.setDateTime(event.getDateTime());

        when(eventMapper.createEventRequestToEvent(request)).thenReturn(event);
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.eventToEventResponse(event)).thenReturn(eventResponse);

        EventResponse response = eventService.createEvent(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Concert");
        assertThat(response.getCategory()).isEqualTo("Music");
        assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(response.getTotalStock()).isEqualTo(50);
        assertThat(response.getAvailableStock()).isEqualTo(50);

        verify(eventMapper).createEventRequestToEvent(request);
        verify(eventRepository).save(event);
        verify(eventMapper).eventToEventResponse(event);
    }

    @Test
    void deleteEvent_whenEventExistsAndNoPurchases_shouldDeleteEvent() {
        event.setTotalStock(50);
        event.setAvailableStock(50);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        eventService.deleteEvent(1L);

        verify(eventRepository).findById(1L);
        verify(eventRepository).delete(event);
    }

    @Test
    void deleteEvent_whenEventNotFound_shouldThrowException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.deleteEvent(99L))
                .isInstanceOf(BaseException.class);

        verify(eventRepository).findById(99L);
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void deleteEvent_whenEventHasPurchases_shouldThrowException() {
        event.setTotalStock(50);
        event.setAvailableStock(40);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.deleteEvent(1L))
                .isInstanceOf(BaseException.class);

        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void getSalesDashboard_shouldReturnCalculatedDashboard() {
        Event event1 = new Event();
        event1.setId(1L);
        event1.setName("Concert");
        event1.setCategory("Music");
        event1.setPrice(BigDecimal.valueOf(100));
        event1.setTotalStock(50);
        event1.setAvailableStock(40);

        Event event2 = new Event();
        event2.setId(2L);
        event2.setName("Theatre");
        event2.setCategory("Art");
        event2.setPrice(BigDecimal.valueOf(200));
        event2.setTotalStock(30);
        event2.setAvailableStock(25);

        EventSalesResponse salesResponse1 = EventSalesResponse.builder()
                .eventId(1L)
                .eventName("Concert")
                .build();

        EventSalesResponse salesResponse2 = EventSalesResponse.builder()
                .eventId(2L)
                .eventName("Theatre")
                .build();

        when(eventRepository.findAll()).thenReturn(List.of(event1, event2));
        when(eventMapper.eventToEventSalesResponse(event1)).thenReturn(salesResponse1);
        when(eventMapper.eventToEventSalesResponse(event2)).thenReturn(salesResponse2);

        SalesDashboardResponse response = eventService.getSalesDashboard();

        assertThat(response).isNotNull();
        assertThat(response.getTotalEventCount()).isEqualTo(2);
        assertThat(response.getTotalSoldTickets()).isEqualTo(15);
        assertThat(response.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(response.getEvents()).hasSize(2);

        assertThat(response.getEvents().get(0).getSoldTickets()).isEqualTo(10);
        assertThat(response.getEvents().get(0).getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(1000));

        assertThat(response.getEvents().get(1).getSoldTickets()).isEqualTo(5);
        assertThat(response.getEvents().get(1).getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(1000));

        verify(eventRepository).findAll();
        verify(eventMapper).eventToEventSalesResponse(event1);
        verify(eventMapper).eventToEventSalesResponse(event2);
    }

    @Test
    void getSalesDashboard_whenThereAreNoEvents_shouldReturnEmptyDashboard() {
        when(eventRepository.findAll()).thenReturn(List.of());

        SalesDashboardResponse response = eventService.getSalesDashboard();

        assertThat(response).isNotNull();
        assertThat(response.getTotalEventCount()).isEqualTo(0);
        assertThat(response.getTotalSoldTickets()).isEqualTo(0);
        assertThat(response.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getEvents()).isEmpty();

        verify(eventRepository).findAll();
        verify(eventMapper, never()).eventToEventSalesResponse(any(Event.class));
    }

    @Test
    void getEvents_whenNameAndCategoryGiven_shouldUseNameAndCategoryFilter() {
        when(eventRepository.findByNameContainingIgnoreCaseAndCategoryIgnoreCaseOrderByDateTimeAsc("con", "Music"))
                .thenReturn(List.of(event));
        when(eventMapper.eventToEventResponse(event)).thenReturn(eventResponse);

        List<EventResponse> responses = eventService.getEvents("con", "Music");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);

        verify(eventRepository)
                .findByNameContainingIgnoreCaseAndCategoryIgnoreCaseOrderByDateTimeAsc("con", "Music");
        verify(eventMapper).eventToEventResponse(event);
    }

    @Test
    void getEvents_whenOnlyNameGiven_shouldUseNameFilter() {
        when(eventRepository.findByNameContainingIgnoreCaseOrderByDateTimeAsc("con"))
                .thenReturn(List.of(event));
        when(eventMapper.eventToEventResponse(event)).thenReturn(eventResponse);

        List<EventResponse> responses = eventService.getEvents("con", null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Concert");

        verify(eventRepository).findByNameContainingIgnoreCaseOrderByDateTimeAsc("con");
        verify(eventMapper).eventToEventResponse(event);
    }

    @Test
    void getEvents_whenOnlyCategoryGiven_shouldUseCategoryFilter() {
        when(eventRepository.findByCategoryIgnoreCaseOrderByDateTimeAsc("Music"))
                .thenReturn(List.of(event));
        when(eventMapper.eventToEventResponse(event)).thenReturn(eventResponse);

        List<EventResponse> responses = eventService.getEvents(null, "Music");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCategory()).isEqualTo("Music");

        verify(eventRepository).findByCategoryIgnoreCaseOrderByDateTimeAsc("Music");
        verify(eventMapper).eventToEventResponse(event);
    }

    @Test
    void getEvents_whenNoFilterGiven_shouldReturnAllEventsOrderedByDate() {
        when(eventRepository.findAllByOrderByDateTimeAsc()).thenReturn(List.of(event));
        when(eventMapper.eventToEventResponse(event)).thenReturn(eventResponse);

        List<EventResponse> responses = eventService.getEvents(null, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);

        verify(eventRepository).findAllByOrderByDateTimeAsc();
        verify(eventMapper).eventToEventResponse(event);
    }

    @Test
    void getEvents_whenBlankNameAndBlankCategoryGiven_shouldReturnAllEventsOrderedByDate() {
        when(eventRepository.findAllByOrderByDateTimeAsc()).thenReturn(List.of(event));
        when(eventMapper.eventToEventResponse(event)).thenReturn(eventResponse);

        List<EventResponse> responses = eventService.getEvents("   ", "   ");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);

        verify(eventRepository).findAllByOrderByDateTimeAsc();
        verify(eventMapper).eventToEventResponse(event);
    }
}