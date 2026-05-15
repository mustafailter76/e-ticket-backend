package com.mustafa_mert.backend.ticket_purchase;

import com.mustafa_mert.backend.common.exception.BaseException;
import com.mustafa_mert.backend.event.entity.Event;
import com.mustafa_mert.backend.event.repository.EventRepository;
import com.mustafa_mert.backend.ticket_purchase.dto.PurchaseTicketRequest;
import com.mustafa_mert.backend.ticket_purchase.dto.TicketPurchaseResponse;
import com.mustafa_mert.backend.ticket_purchase.entity.TicketPurchase;
import com.mustafa_mert.backend.ticket_purchase.repository.TicketPurchaseRepository;
import com.mustafa_mert.backend.ticket_purchase.service.TicketPurchaseServiceImpl;
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
class TicketPurchaseServiceImplTest {

    @Mock
    private TicketPurchaseRepository ticketPurchaseRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketPurchaseServiceImpl ticketPurchaseService;

    private User normalUser;
    private User adminUser;
    private Event event;
    private TicketPurchase ticketPurchase;
    private PurchaseTicketRequest purchaseTicketRequest;

    @BeforeEach
    void setUp() {
        normalUser = User.builder()
                .id(1L)
                .email("user@gmail.com")
                .firstName("Mustafa")
                .lastName("Ilter")
                .role("USER")
                .passwordHash("encodedPassword")
                .build();

        adminUser = User.builder()
                .id(2L)
                .email("admin@gmail.com")
                .firstName("Admin")
                .lastName("User")
                .role("ADMIN")
                .passwordHash("encodedPassword")
                .build();

        event = Event.builder()
                .id(1L)
                .name("Rock Concert")
                .category("Music")
                .description("Rock music event")
                .dateTime(LocalDateTime.now().plusDays(10))
                .location("Istanbul")
                .price(new BigDecimal("500.00"))
                .totalStock(100)
                .availableStock(80)
                .build();

        purchaseTicketRequest = new PurchaseTicketRequest();
        purchaseTicketRequest.setEventId(1L);
        purchaseTicketRequest.setQuantity(2);

        ticketPurchase = TicketPurchase.builder()
                .id(1L)
                .user(normalUser)
                .event(event)
                .quantity(2)
                .totalPrice(new BigDecimal("1000.00"))
                .purchasedAt(LocalDateTime.now())
                .build();
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
    void purchaseTicket_WhenUserAndEventAreValid_ShouldPurchaseTicketAndReturnResponse() {
        setAuthenticatedUser("user@gmail.com");

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(ticketPurchaseRepository.save(any(TicketPurchase.class))).thenReturn(ticketPurchase);

        TicketPurchaseResponse response = ticketPurchaseService.purchaseTicket(purchaseTicketRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(2, response.getQuantity());
        assertEquals(new BigDecimal("1000.00"), response.getTotalPrice());

        assertNotNull(response.getEvent());
        assertEquals(event.getId(), response.getEvent().getId());
        assertEquals(event.getName(), response.getEvent().getName());
        assertEquals(event.getCategory(), response.getEvent().getCategory());
        assertEquals(78, response.getEvent().getAvailableStock());

        assertNotNull(response.getUser());
        assertEquals(normalUser.getId(), response.getUser().getId());
        assertEquals(normalUser.getEmail(), response.getUser().getEmail());
        assertEquals(normalUser.getRole(), response.getUser().getRole());

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).save(event);
        verify(ticketPurchaseRepository, times(1)).save(any(TicketPurchase.class));
    }

    @Test
    void purchaseTicket_WhenCurrentUserIsAdmin_ShouldThrowBaseException() {
        setAuthenticatedUser("admin@gmail.com");

        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(adminUser));

        assertThrows(BaseException.class, () -> ticketPurchaseService.purchaseTicket(purchaseTicketRequest));

        verify(userRepository, times(1)).findByEmail("admin@gmail.com");
        verify(eventRepository, never()).findById(anyLong());
        verify(eventRepository, never()).save(any(Event.class));
        verify(ticketPurchaseRepository, never()).save(any(TicketPurchase.class));
    }

    @Test
    void purchaseTicket_WhenCurrentUserNotFound_ShouldThrowBaseException() {
        setAuthenticatedUser("unknown@gmail.com");

        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> ticketPurchaseService.purchaseTicket(purchaseTicketRequest));

        verify(userRepository, times(1)).findByEmail("unknown@gmail.com");
        verify(eventRepository, never()).findById(anyLong());
        verify(ticketPurchaseRepository, never()).save(any(TicketPurchase.class));
    }

    @Test
    void purchaseTicket_WhenEventNotFound_ShouldThrowBaseException() {
        setAuthenticatedUser("user@gmail.com");

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> ticketPurchaseService.purchaseTicket(purchaseTicketRequest));

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
        verify(ticketPurchaseRepository, never()).save(any(TicketPurchase.class));
    }

    @Test
    void purchaseTicket_WhenNotEnoughTickets_ShouldThrowBaseException() {
        setAuthenticatedUser("user@gmail.com");

        purchaseTicketRequest.setQuantity(100);

        Event lowStockEvent = Event.builder()
                .id(1L)
                .name("Rock Concert")
                .category("Music")
                .description("Rock music event")
                .dateTime(LocalDateTime.now().plusDays(10))
                .location("Istanbul")
                .price(new BigDecimal("500.00"))
                .totalStock(100)
                .availableStock(5)
                .build();

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(lowStockEvent));

        assertThrows(BaseException.class, () -> ticketPurchaseService.purchaseTicket(purchaseTicketRequest));

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
        verify(ticketPurchaseRepository, never()).save(any(TicketPurchase.class));
    }

    @Test
    void cancelTicket_WhenUserAndTicketAreValid_ShouldCancelTicket() {
        setAuthenticatedUser("user@gmail.com");

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));
        when(ticketPurchaseRepository.findById(1L)).thenReturn(Optional.of(ticketPurchase));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        ticketPurchaseService.cancelTicket(1L);

        assertEquals(82, event.getAvailableStock());

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(ticketPurchaseRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).save(event);
        verify(ticketPurchaseRepository, times(1)).delete(ticketPurchase);
    }

    @Test
    void cancelTicket_WhenCurrentUserIsAdmin_ShouldThrowBaseException() {
        setAuthenticatedUser("admin@gmail.com");

        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(adminUser));

        assertThrows(BaseException.class, () -> ticketPurchaseService.cancelTicket(1L));

        verify(userRepository, times(1)).findByEmail("admin@gmail.com");
        verify(ticketPurchaseRepository, never()).findById(anyLong());
        verify(eventRepository, never()).save(any(Event.class));
        verify(ticketPurchaseRepository, never()).delete(any(TicketPurchase.class));
    }

    @Test
    void cancelTicket_WhenTicketPurchaseNotFound_ShouldThrowBaseException() {
        setAuthenticatedUser("user@gmail.com");

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));
        when(ticketPurchaseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> ticketPurchaseService.cancelTicket(1L));

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(ticketPurchaseRepository, times(1)).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
        verify(ticketPurchaseRepository, never()).delete(any(TicketPurchase.class));
    }

    @Test
    void cancelTicket_WhenEventDateIsPast_ShouldThrowBaseException() {
        setAuthenticatedUser("user@gmail.com");

        Event pastEvent = Event.builder()
                .id(1L)
                .name("Old Concert")
                .category("Music")
                .description("Past event")
                .dateTime(LocalDateTime.now().minusDays(1))
                .location("Istanbul")
                .price(new BigDecimal("500.00"))
                .totalStock(100)
                .availableStock(80)
                .build();

        TicketPurchase pastTicketPurchase = TicketPurchase.builder()
                .id(1L)
                .user(normalUser)
                .event(pastEvent)
                .quantity(2)
                .totalPrice(new BigDecimal("1000.00"))
                .purchasedAt(LocalDateTime.now().minusDays(5))
                .build();

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));
        when(ticketPurchaseRepository.findById(1L)).thenReturn(Optional.of(pastTicketPurchase));

        assertThrows(BaseException.class, () -> ticketPurchaseService.cancelTicket(1L));

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(ticketPurchaseRepository, times(1)).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
        verify(ticketPurchaseRepository, never()).delete(any(TicketPurchase.class));
    }

    @Test
    void getAllPurchasedTickets_WhenUserHasTickets_ShouldReturnTicketPurchaseResponses() {
        setAuthenticatedUser("user@gmail.com");

        TicketPurchase ticketPurchase2 = TicketPurchase.builder()
                .id(2L)
                .user(normalUser)
                .event(event)
                .quantity(3)
                .totalPrice(new BigDecimal("1500.00"))
                .purchasedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));
        when(ticketPurchaseRepository.findByUserId(1L))
                .thenReturn(Arrays.asList(ticketPurchase, ticketPurchase2));

        List<TicketPurchaseResponse> responses = ticketPurchaseService.getAllPurchasedTickets();

        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).getId());
        assertEquals(2, responses.get(0).getQuantity());
        assertEquals(new BigDecimal("1000.00"), responses.get(0).getTotalPrice());
        assertEquals(event.getName(), responses.get(0).getEvent().getName());
        assertEquals(normalUser.getEmail(), responses.get(0).getUser().getEmail());

        assertEquals(2L, responses.get(1).getId());
        assertEquals(3, responses.get(1).getQuantity());
        assertEquals(new BigDecimal("1500.00"), responses.get(1).getTotalPrice());

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(ticketPurchaseRepository, times(1)).findByUserId(1L);
    }

    @Test
    void getAllPurchasedTickets_WhenCurrentUserIsAdmin_ShouldThrowBaseException() {
        setAuthenticatedUser("admin@gmail.com");

        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(adminUser));

        assertThrows(BaseException.class, () -> ticketPurchaseService.getAllPurchasedTickets());

        verify(userRepository, times(1)).findByEmail("admin@gmail.com");
        verify(ticketPurchaseRepository, never()).findByUserId(anyLong());
    }

    @Test
    void getAllPurchasedTickets_WhenTicketListIsEmpty_ShouldThrowBaseException() {
        setAuthenticatedUser("user@gmail.com");

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));
        when(ticketPurchaseRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        assertThrows(BaseException.class, () -> ticketPurchaseService.getAllPurchasedTickets());

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(ticketPurchaseRepository, times(1)).findByUserId(1L);
    }
}