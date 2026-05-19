package com.mustafa_mert.backend.ticket_purchase;

import com.mustafa_mert.backend.common.exception.BaseException;
import com.mustafa_mert.backend.event.dto.EventResponse;
import com.mustafa_mert.backend.event.entity.Event;
import com.mustafa_mert.backend.event.mapper.EventMapper;
import com.mustafa_mert.backend.event.repository.EventRepository;
import com.mustafa_mert.backend.security.CurrentUserProvider;
import com.mustafa_mert.backend.ticket_purchase.dto.PurchaseTicketRequest;
import com.mustafa_mert.backend.ticket_purchase.dto.TicketPurchaseResponse;
import com.mustafa_mert.backend.ticket_purchase.entity.TicketPurchase;
import com.mustafa_mert.backend.ticket_purchase.repository.TicketPurchaseRepository;
import com.mustafa_mert.backend.ticket_purchase.service.TicketPurchaseServiceImpl;
import com.mustafa_mert.backend.user.entity.User;
import com.mustafa_mert.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class TicketPurchaseServiceImplTest {

    @Mock
    private TicketPurchaseRepository ticketPurchaseRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private TicketPurchaseServiceImpl ticketPurchaseService;

    private User user;
    private Event event;
    private EventResponse eventResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setFirstName("Mustafa");
        user.setLastName("Ilter");

        event = new Event();
        event.setId(10L);
        event.setName("Concert");
        event.setPrice(BigDecimal.valueOf(100));
        event.setAvailableStock(20);
        event.setTotalStock(50);
        event.setDateTime(LocalDateTime.now().plusDays(5));

        eventResponse = EventResponse.builder()
                .id(10L)
                .name("Concert")
                .price(BigDecimal.valueOf(100))
                .availableStock(20)
                .totalStock(50)
                .dateTime(event.getDateTime())
                .build();
    }

    @Test
    void purchaseTicket_whenEventExistsAndStockIsEnough_shouldPurchaseTicket() {
        PurchaseTicketRequest request = new PurchaseTicketRequest();
        request.setEventId(10L);
        request.setQuantity(2);

        TicketPurchase savedPurchase = TicketPurchase.builder()
                .id(100L)
                .user(user)
                .event(event)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(200))
                .purchasedAt(LocalDateTime.now())
                .build();

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(ticketPurchaseRepository.save(any(TicketPurchase.class))).thenReturn(savedPurchase);
        when(eventMapper.eventToEventResponse(event)).thenReturn(eventResponse);

        TicketPurchaseResponse response = ticketPurchaseService.purchaseTicket(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(response.getUser().getId()).isEqualTo(1L);
        assertThat(response.getEvent().getId()).isEqualTo(10L);

        assertThat(event.getAvailableStock()).isEqualTo(18);

        verify(currentUserProvider).getCurrentUser();
        verify(eventRepository).findById(10L);
        verify(eventRepository).save(event);
        verify(ticketPurchaseRepository).save(any(TicketPurchase.class));
        verify(eventMapper).eventToEventResponse(event);
    }

    @Test
    void purchaseTicket_whenEventNotFound_shouldThrowException() {
        PurchaseTicketRequest request = new PurchaseTicketRequest();
        request.setEventId(99L);
        request.setQuantity(2);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketPurchaseService.purchaseTicket(request))
                .isInstanceOf(BaseException.class);

        verify(currentUserProvider).getCurrentUser();
        verify(eventRepository).findById(99L);
        verify(eventRepository, never()).save(any(Event.class));
        verify(ticketPurchaseRepository, never()).save(any(TicketPurchase.class));
    }

    @Test
    void purchaseTicket_whenStockIsNotEnough_shouldThrowException() {
        PurchaseTicketRequest request = new PurchaseTicketRequest();
        request.setEventId(10L);
        request.setQuantity(30);

        event.setAvailableStock(5);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> ticketPurchaseService.purchaseTicket(request))
                .isInstanceOf(BaseException.class);

        assertThat(event.getAvailableStock()).isEqualTo(5);

        verify(currentUserProvider).getCurrentUser();
        verify(eventRepository).findById(10L);
        verify(eventRepository, never()).save(any(Event.class));
        verify(ticketPurchaseRepository, never()).save(any(TicketPurchase.class));
    }

    @Test
    void purchaseTicket_shouldSaveTicketPurchaseWithCorrectValues() {
        PurchaseTicketRequest request = new PurchaseTicketRequest();
        request.setEventId(10L);
        request.setQuantity(3);

        TicketPurchase savedPurchase = TicketPurchase.builder()
                .id(100L)
                .user(user)
                .event(event)
                .quantity(3)
                .totalPrice(BigDecimal.valueOf(300))
                .purchasedAt(LocalDateTime.now())
                .build();

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(ticketPurchaseRepository.save(any(TicketPurchase.class))).thenReturn(savedPurchase);
        when(eventMapper.eventToEventResponse(event)).thenReturn(eventResponse);

        ticketPurchaseService.purchaseTicket(request);

        ArgumentCaptor<TicketPurchase> captor = ArgumentCaptor.forClass(TicketPurchase.class);
        verify(ticketPurchaseRepository).save(captor.capture());

        TicketPurchase capturedPurchase = captor.getValue();

        assertThat(capturedPurchase.getUser()).isEqualTo(user);
        assertThat(capturedPurchase.getEvent()).isEqualTo(event);
        assertThat(capturedPurchase.getQuantity()).isEqualTo(3);
        assertThat(capturedPurchase.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(capturedPurchase.getPurchasedAt()).isNotNull();
    }

    @Test
    void cancelTicket_whenTicketExistsAndEventIsFuture_shouldCancelTicket() {
        TicketPurchase ticketPurchase = TicketPurchase.builder()
                .id(100L)
                .user(user)
                .event(event)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(200))
                .purchasedAt(LocalDateTime.now())
                .build();

        event.setAvailableStock(18);
        event.setDateTime(LocalDateTime.now().plusDays(5));

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(ticketPurchaseRepository.findById(100L)).thenReturn(Optional.of(ticketPurchase));

        ticketPurchaseService.cancelTicket(100L);

        assertThat(event.getAvailableStock()).isEqualTo(20);

        verify(currentUserProvider).getCurrentUser();
        verify(ticketPurchaseRepository).findById(100L);
        verify(eventRepository).save(event);
        verify(ticketPurchaseRepository).delete(ticketPurchase);
    }

    @Test
    void cancelTicket_whenTicketNotFound_shouldThrowException() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(ticketPurchaseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketPurchaseService.cancelTicket(999L))
                .isInstanceOf(BaseException.class);

        verify(currentUserProvider).getCurrentUser();
        verify(ticketPurchaseRepository).findById(999L);
        verify(eventRepository, never()).save(any(Event.class));
        verify(ticketPurchaseRepository, never()).delete(any(TicketPurchase.class));
    }

    @Test
    void cancelTicket_whenEventDatePassed_shouldThrowException() {
        event.setDateTime(LocalDateTime.now().minusDays(1));

        TicketPurchase ticketPurchase = TicketPurchase.builder()
                .id(100L)
                .user(user)
                .event(event)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(200))
                .purchasedAt(LocalDateTime.now().minusDays(3))
                .build();

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(ticketPurchaseRepository.findById(100L)).thenReturn(Optional.of(ticketPurchase));

        assertThatThrownBy(() -> ticketPurchaseService.cancelTicket(100L))
                .isInstanceOf(BaseException.class);

        verify(currentUserProvider).getCurrentUser();
        verify(ticketPurchaseRepository).findById(100L);
        verify(eventRepository, never()).save(any(Event.class));
        verify(ticketPurchaseRepository, never()).delete(any(TicketPurchase.class));
    }

    @Test
    void getAllPurchasedTickets_whenUserHasTickets_shouldReturnTicketList() {
        TicketPurchase ticketPurchase = TicketPurchase.builder()
                .id(100L)
                .user(user)
                .event(event)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(200))
                .purchasedAt(LocalDateTime.now())
                .build();

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(ticketPurchaseRepository.findByUserId(1L)).thenReturn(List.of(ticketPurchase));
        when(eventMapper.eventToEventResponse(event)).thenReturn(eventResponse);

        List<TicketPurchaseResponse> responses = ticketPurchaseService.getAllPurchasedTickets();

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);

        TicketPurchaseResponse response = responses.get(0);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(response.getUser().getId()).isEqualTo(1L);
        assertThat(response.getUser().getEmail()).isEqualTo("test@gmail.com");
        assertThat(response.getEvent().getId()).isEqualTo(10L);

        verify(currentUserProvider).getCurrentUser();
        verify(ticketPurchaseRepository).findByUserId(1L);
        verify(eventMapper).eventToEventResponse(event);
    }

    @Test
    void getAllPurchasedTickets_whenUserHasNoTickets_shouldThrowException() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(ticketPurchaseRepository.findByUserId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> ticketPurchaseService.getAllPurchasedTickets())
                .isInstanceOf(BaseException.class);

        verify(currentUserProvider).getCurrentUser();
        verify(ticketPurchaseRepository).findByUserId(1L);
        verify(eventMapper, never()).eventToEventResponse(any(Event.class));
    }
}