package com.mustafa_mert.backend.event;

import com.mustafa_mert.backend.common.exception.BaseException;
import com.mustafa_mert.backend.event.dto.CreateEventRequest;
import com.mustafa_mert.backend.event.dto.EventResponse;
import com.mustafa_mert.backend.event.dto.SalesDashboardResponse;
import com.mustafa_mert.backend.event.entity.Event;
import com.mustafa_mert.backend.event.repository.EventRepository;
import com.mustafa_mert.backend.event.service.EventServiceImpl;
import com.mustafa_mert.backend.user.entity.User;
import com.mustafa_mert.backend.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private User adminUser;
    private User normalUser;
    private Event event;
    private CreateEventRequest createEventRequest;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .email("admin@gmail.com")
                .firstName("Admin")
                .lastName("User")
                .role("ADMIN")
                .passwordHash("encodedPassword")
                .build();

        normalUser = User.builder()
                .id(2L)
                .email("user@gmail.com")
                .firstName("Normal")
                .lastName("User")
                .role("USER")
                .passwordHash("encodedPassword")
                .build();

        event = Event.builder()
                .id(1L)
                .name("Rock Concert")
                .category("Music")
                .description("Rock music event")
                .dateTime(LocalDateTime.of(2026, 6, 10, 20, 0))
                .location("Istanbul")
                .price(new BigDecimal("500.00"))
                .totalStock(100)
                .availableStock(100)
                .build();

        createEventRequest = new CreateEventRequest();
        createEventRequest.setName("Rock Concert");
        createEventRequest.setCategory("Music");
        createEventRequest.setDescription("Rock music event");
        createEventRequest.setDateTime(LocalDateTime.of(2026, 6, 10, 20, 0));
        createEventRequest.setLocation("Istanbul");
        createEventRequest.setPrice(new BigDecimal("500.00"));
        createEventRequest.setTotalStock(100);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUser(String email) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createEvent_WhenUserIsAdmin_ShouldCreateEventAndReturnEventResponse() {
        setAuthenticatedUser("admin@gmail.com");

        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(adminUser));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventResponse response = eventService.createEvent(createEventRequest);

        assertNotNull(response);
        assertEquals(event.getId(), response.getId());
        assertEquals(event.getName(), response.getName());
        assertEquals(event.getCategory(), response.getCategory());
        assertEquals(event.getDescription(), response.getDescription());
        assertEquals(event.getDateTime(), response.getDateTime());
        assertEquals(event.getLocation(), response.getLocation());
        assertEquals(event.getPrice(), response.getPrice());
        assertEquals(event.getTotalStock(), response.getTotalStock());
        assertEquals(event.getAvailableStock(), response.getAvailableStock());

        verify(userRepository, times(1)).findByEmail("admin@gmail.com");
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void createEvent_WhenUserIsNormalUser_ShouldThrowBaseException() {
        setAuthenticatedUser("user@gmail.com");

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));

        assertThrows(BaseException.class, () -> eventService.createEvent(createEventRequest));

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEvent_WhenCurrentUserNotFound_ShouldThrowBaseException() {
        setAuthenticatedUser("unknown@gmail.com");

        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> eventService.createEvent(createEventRequest));

        verify(userRepository, times(1)).findByEmail("unknown@gmail.com");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void deleteEvent_WhenUserIsAdminAndEventHasNoPurchases_ShouldDeleteEvent() {
        setAuthenticatedUser("admin@gmail.com");

        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(adminUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        eventService.deleteEvent(1L);

        verify(userRepository, times(1)).findByEmail("admin@gmail.com");
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).delete(event);
    }

    @Test
    void deleteEvent_WhenUserIsNormalUser_ShouldThrowBaseException() {
        setAuthenticatedUser("user@gmail.com");

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));

        assertThrows(BaseException.class, () -> eventService.deleteEvent(1L));

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(eventRepository, never()).findById(anyLong());
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void deleteEvent_WhenEventNotFound_ShouldThrowBaseException() {
        setAuthenticatedUser("admin@gmail.com");

        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(adminUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> eventService.deleteEvent(1L));

        verify(userRepository, times(1)).findByEmail("admin@gmail.com");
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void deleteEvent_WhenEventHasPurchases_ShouldThrowBaseException() {
        setAuthenticatedUser("admin@gmail.com");

        Event purchasedEvent = Event.builder()
                .id(1L)
                .name("Rock Concert")
                .category("Music")
                .description("Rock music event")
                .dateTime(LocalDateTime.of(2026, 6, 10, 20, 0))
                .location("Istanbul")
                .price(new BigDecimal("500.00"))
                .totalStock(100)
                .availableStock(80)
                .build();

        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(adminUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(purchasedEvent));

        assertThrows(BaseException.class, () -> eventService.deleteEvent(1L));

        verify(userRepository, times(1)).findByEmail("admin@gmail.com");
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void getSalesDashboard_WhenUserIsAdmin_ShouldReturnDashboardResponse() {
        setAuthenticatedUser("admin@gmail.com");

        Event event1 = Event.builder()
                .id(1L)
                .name("Rock Concert")
                .category("Music")
                .description("Rock music event")
                .dateTime(LocalDateTime.of(2026, 6, 10, 20, 0))
                .location("Istanbul")
                .price(new BigDecimal("500.00"))
                .totalStock(100)
                .availableStock(80)
                .build();

        Event event2 = Event.builder()
                .id(2L)
                .name("Football Match")
                .category("Sport")
                .description("Football event")
                .dateTime(LocalDateTime.of(2026, 7, 5, 21, 0))
                .location("Kadikoy")
                .price(new BigDecimal("300.00"))
                .totalStock(200)
                .availableStock(150)
                .build();

        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(adminUser));
        when(eventRepository.findAll()).thenReturn(Arrays.asList(event1, event2));

        SalesDashboardResponse response = eventService.getSalesDashboard();

        assertNotNull(response);
        assertEquals(2, response.getTotalEventCount());
        assertEquals(70, response.getTotalSoldTickets());
        assertEquals(new BigDecimal("25000.00"), response.getTotalRevenue());
        assertEquals(2, response.getEvents().size());

        assertEquals(1L, response.getEvents().get(0).getEventId());
        assertEquals("Rock Concert", response.getEvents().get(0).getEventName());
        assertEquals(20, response.getEvents().get(0).getSoldTickets());
        assertEquals(new BigDecimal("10000.00"), response.getEvents().get(0).getTotalRevenue());

        assertEquals(2L, response.getEvents().get(1).getEventId());
        assertEquals("Football Match", response.getEvents().get(1).getEventName());
        assertEquals(50, response.getEvents().get(1).getSoldTickets());
        assertEquals(new BigDecimal("15000.00"), response.getEvents().get(1).getTotalRevenue());

        verify(userRepository, times(1)).findByEmail("admin@gmail.com");
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void getSalesDashboard_WhenUserIsNotAdmin_ShouldThrowBaseException() {
        setAuthenticatedUser("user@gmail.com");

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));

        assertThrows(BaseException.class, () -> eventService.getSalesDashboard());

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(eventRepository, never()).findAll();
    }

    @Test
    void getEvents_WhenNameAndCategoryAreGiven_ShouldReturnFilteredEvents() {
        when(eventRepository.findByNameContainingIgnoreCaseAndCategoryIgnoreCaseOrderByDateTimeAsc("Rock", "Music"))
                .thenReturn(Collections.singletonList(event));

        List<EventResponse> responses = eventService.getEvents("Rock", "Music");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(event.getId(), responses.get(0).getId());
        assertEquals(event.getName(), responses.get(0).getName());
        assertEquals(event.getCategory(), responses.get(0).getCategory());

        verify(eventRepository, times(1))
                .findByNameContainingIgnoreCaseAndCategoryIgnoreCaseOrderByDateTimeAsc("Rock", "Music");
        verify(eventRepository, never()).findAllByOrderByDateTimeAsc();
    }

    @Test
    void getEvents_WhenOnlyNameIsGiven_ShouldReturnEventsByName() {
        when(eventRepository.findByNameContainingIgnoreCaseOrderByDateTimeAsc("Rock"))
                .thenReturn(Collections.singletonList(event));

        List<EventResponse> responses = eventService.getEvents("Rock", null);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Rock Concert", responses.get(0).getName());

        verify(eventRepository, times(1)).findByNameContainingIgnoreCaseOrderByDateTimeAsc("Rock");
    }

    @Test
    void getEvents_WhenOnlyCategoryIsGiven_ShouldReturnEventsByCategory() {
        when(eventRepository.findByCategoryIgnoreCaseOrderByDateTimeAsc("Music"))
                .thenReturn(Collections.singletonList(event));

        List<EventResponse> responses = eventService.getEvents(null, "Music");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Music", responses.get(0).getCategory());

        verify(eventRepository, times(1)).findByCategoryIgnoreCaseOrderByDateTimeAsc("Music");
    }

    @Test
    void getEvents_WhenNameAndCategoryAreNull_ShouldReturnAllEvents() {
        when(eventRepository.findAllByOrderByDateTimeAsc())
                .thenReturn(Collections.singletonList(event));

        List<EventResponse> responses = eventService.getEvents(null, null);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(event.getId(), responses.get(0).getId());

        verify(eventRepository, times(1)).findAllByOrderByDateTimeAsc();
    }

    @Test
    void getEvents_WhenNameAndCategoryAreBlank_ShouldReturnAllEvents() {
        when(eventRepository.findAllByOrderByDateTimeAsc())
                .thenReturn(Collections.singletonList(event));

        List<EventResponse> responses = eventService.getEvents("   ", "   ");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(event.getName(), responses.get(0).getName());

        verify(eventRepository, times(1)).findAllByOrderByDateTimeAsc();
    }
}
