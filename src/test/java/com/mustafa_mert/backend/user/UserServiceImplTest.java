package com.mustafa_mert.backend.user;

import com.mustafa_mert.backend.common.exception.BaseException;
import com.mustafa_mert.backend.event.entity.Event;
import com.mustafa_mert.backend.event.repository.EventRepository;
import com.mustafa_mert.backend.ticket_purchase.entity.TicketPurchase;
import com.mustafa_mert.backend.ticket_purchase.repository.TicketPurchaseRepository;
import com.mustafa_mert.backend.user.dto.ChangePasswordRequest;
import com.mustafa_mert.backend.user.dto.UserResponse;
import com.mustafa_mert.backend.user.entity.User;
import com.mustafa_mert.backend.user.repository.UserRepository;
import com.mustafa_mert.backend.user.service.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketPurchaseRepository ticketPurchaseRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User normalUser;
    private User adminUser;
    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        normalUser = User.builder()
                .id(1L)
                .email("user@gmail.com")
                .firstName("Mustafa")
                .lastName("Ilter")
                .role("USER")
                .passwordHash("oldEncodedPassword")
                .build();

        adminUser = User.builder()
                .id(2L)
                .email("admin@gmail.com")
                .firstName("Admin")
                .lastName("User")
                .role("ADMIN")
                .passwordHash("adminEncodedPassword")
                .build();

        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setPassword("newPassword");
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
    void getMe_WhenCurrentUserExists_ShouldReturnUserResponse() {
        setAuthenticatedUser("user@gmail.com");

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));

        UserResponse response = userService.getMe();

        assertNotNull(response);
        assertEquals(normalUser.getId(), response.getId());
        assertEquals(normalUser.getEmail(), response.getEmail());
        assertEquals(normalUser.getFirstName(), response.getFirstName());
        assertEquals(normalUser.getLastName(), response.getLastName());
        assertEquals(normalUser.getRole(), response.getRole());

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
    }

    @Test
    void getMe_WhenCurrentUserNotFound_ShouldThrowBaseException() {
        setAuthenticatedUser("unknown@gmail.com");

        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> userService.getMe());

        verify(userRepository, times(1)).findByEmail("unknown@gmail.com");
    }

    @Test
    void changePassword_WhenPasswordIsValid_ShouldChangePassword() {
        setAuthenticatedUser("user@gmail.com");

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.matches("newPassword", normalUser.getPasswordHash())).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(normalUser)).thenReturn(normalUser);

        userService.changePassword(changePasswordRequest);

        assertEquals("newEncodedPassword", normalUser.getPasswordHash());

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(passwordEncoder, times(1)).matches("newPassword", "oldEncodedPassword");
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(normalUser);
    }

    @Test
    void changePassword_WhenPasswordMatchesOldPassword_ShouldThrowBaseException() {
        setAuthenticatedUser("user@gmail.com");

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.matches("newPassword", normalUser.getPasswordHash())).thenReturn(true);

        assertThrows(BaseException.class, () -> userService.changePassword(changePasswordRequest));

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(passwordEncoder, times(1)).matches("newPassword", "oldEncodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_WhenCurrentUserNotFound_ShouldThrowBaseException() {
        setAuthenticatedUser("unknown@gmail.com");

        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> userService.changePassword(changePasswordRequest));

        verify(userRepository, times(1)).findByEmail("unknown@gmail.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteMe_WhenUserHasFutureTickets_ShouldIncreaseAvailableStockAndDeleteUser() {
        setAuthenticatedUser("user@gmail.com");

        Event futureEvent1 = Event.builder()
                .id(1L)
                .name("Rock Concert")
                .category("Music")
                .description("Rock event")
                .dateTime(LocalDateTime.now().plusDays(5))
                .location("Istanbul")
                .price(new BigDecimal("500.00"))
                .totalStock(100)
                .availableStock(80)
                .build();

        Event futureEvent2 = Event.builder()
                .id(2L)
                .name("Football Match")
                .category("Sport")
                .description("Football event")
                .dateTime(LocalDateTime.now().plusDays(10))
                .location("Kadikoy")
                .price(new BigDecimal("300.00"))
                .totalStock(200)
                .availableStock(150)
                .build();

        TicketPurchase ticketPurchase1 = TicketPurchase.builder()
                .id(1L)
                .user(normalUser)
                .event(futureEvent1)
                .quantity(2)
                .totalPrice(new BigDecimal("1000.00"))
                .purchasedAt(LocalDateTime.now())
                .build();

        TicketPurchase ticketPurchase2 = TicketPurchase.builder()
                .id(2L)
                .user(normalUser)
                .event(futureEvent2)
                .quantity(3)
                .totalPrice(new BigDecimal("900.00"))
                .purchasedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));
        when(ticketPurchaseRepository.findByUserId(1L))
                .thenReturn(Arrays.asList(ticketPurchase1, ticketPurchase2));

        userService.deleteMe();

        assertEquals(82, futureEvent1.getAvailableStock());
        assertEquals(153, futureEvent2.getAvailableStock());

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(ticketPurchaseRepository, times(1)).findByUserId(1L);
        verify(eventRepository, times(1)).save(futureEvent1);
        verify(eventRepository, times(1)).save(futureEvent2);
        verify(userRepository, times(1)).delete(normalUser);
    }

    @Test
    void deleteMe_WhenUserHasPastTickets_ShouldNotIncreaseAvailableStockButDeleteUser() {
        setAuthenticatedUser("user@gmail.com");

        Event pastEvent = Event.builder()
                .id(1L)
                .name("Old Concert")
                .category("Music")
                .description("Old event")
                .dateTime(LocalDateTime.now().minusDays(2))
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
        when(ticketPurchaseRepository.findByUserId(1L))
                .thenReturn(Collections.singletonList(pastTicketPurchase));

        userService.deleteMe();

        assertEquals(80, pastEvent.getAvailableStock());

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(ticketPurchaseRepository, times(1)).findByUserId(1L);
        verify(eventRepository, never()).save(any(Event.class));
        verify(userRepository, times(1)).delete(normalUser);
    }

    @Test
    void deleteMe_WhenUserHasNoTickets_ShouldDeleteUser() {
        setAuthenticatedUser("user@gmail.com");

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(normalUser));
        when(ticketPurchaseRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        userService.deleteMe();

        verify(userRepository, times(1)).findByEmail("user@gmail.com");
        verify(ticketPurchaseRepository, times(1)).findByUserId(1L);
        verify(eventRepository, never()).save(any(Event.class));
        verify(userRepository, times(1)).delete(normalUser);
    }

    @Test
    void deleteMe_WhenCurrentUserIsAdmin_ShouldThrowBaseException() {
        setAuthenticatedUser("admin@gmail.com");

        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(adminUser));

        assertThrows(BaseException.class, () -> userService.deleteMe());

        verify(userRepository, times(1)).findByEmail("admin@gmail.com");
        verify(ticketPurchaseRepository, never()).findByUserId(anyLong());
        verify(eventRepository, never()).save(any(Event.class));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteMe_WhenCurrentUserNotFound_ShouldThrowBaseException() {
        setAuthenticatedUser("unknown@gmail.com");

        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> userService.deleteMe());

        verify(userRepository, times(1)).findByEmail("unknown@gmail.com");
        verify(ticketPurchaseRepository, never()).findByUserId(anyLong());
        verify(eventRepository, never()).save(any(Event.class));
        verify(userRepository, never()).delete(any(User.class));
    }
}
