package com.mustafa_mert.backend.user;

import com.mustafa_mert.backend.common.exception.BaseException;
import com.mustafa_mert.backend.event.entity.Event;
import com.mustafa_mert.backend.event.repository.EventRepository;
import com.mustafa_mert.backend.security.CurrentUserProvider;
import com.mustafa_mert.backend.ticket_purchase.entity.TicketPurchase;
import com.mustafa_mert.backend.ticket_purchase.repository.TicketPurchaseRepository;
import com.mustafa_mert.backend.user.dto.ChangePasswordRequest;
import com.mustafa_mert.backend.user.dto.UserResponse;
import com.mustafa_mert.backend.user.entity.User;
import com.mustafa_mert.backend.user.repository.UserRepository;
import com.mustafa_mert.backend.user.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setFirstName("Mustafa");
        user.setLastName("Ilter");
        user.setPasswordHash("encodedOldPassword");
    }

    @Test
    void getMe_shouldReturnCurrentUserInformation() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);

        UserResponse response = userService.getMe();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@gmail.com");
        assertThat(response.getFirstName()).isEqualTo("Mustafa");
        assertThat(response.getLastName()).isEqualTo("Ilter");

        verify(currentUserProvider).getCurrentUser();
    }

    @Test
    void changePassword_whenNewPasswordIsDifferent_shouldUpdatePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword("newPassword123");

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("newPassword123", "encodedOldPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        userService.changePassword(request);

        assertThat(user.getPasswordHash()).isEqualTo("encodedNewPassword");

        verify(currentUserProvider).getCurrentUser();
        verify(passwordEncoder).matches("newPassword123", "encodedOldPassword");
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_whenNewPasswordIsSameWithOldPassword_shouldThrowException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword("oldPassword123");

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("oldPassword123", "encodedOldPassword")).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword(request))
                .isInstanceOf(BaseException.class);

        verify(currentUserProvider).getCurrentUser();
        verify(passwordEncoder).matches("oldPassword123", "encodedOldPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteMe_shouldRestoreStockForFutureEventsAndDeleteUser() {
        Event futureEvent = new Event();
        futureEvent.setId(10L);
        futureEvent.setDateTime(LocalDateTime.now().plusDays(3));
        futureEvent.setAvailableStock(20);

        Event pastEvent = new Event();
        pastEvent.setId(20L);
        pastEvent.setDateTime(LocalDateTime.now().minusDays(3));
        pastEvent.setAvailableStock(15);

        TicketPurchase futurePurchase = new TicketPurchase();
        futurePurchase.setId(100L);
        futurePurchase.setUser(user);
        futurePurchase.setEvent(futureEvent);
        futurePurchase.setQuantity(2);

        TicketPurchase pastPurchase = new TicketPurchase();
        pastPurchase.setId(200L);
        pastPurchase.setUser(user);
        pastPurchase.setEvent(pastEvent);
        pastPurchase.setQuantity(5);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(ticketPurchaseRepository.findByUserId(1L))
                .thenReturn(List.of(futurePurchase, pastPurchase));

        userService.deleteMe();

        assertThat(futureEvent.getAvailableStock()).isEqualTo(22);
        assertThat(pastEvent.getAvailableStock()).isEqualTo(15);

        verify(currentUserProvider).getCurrentUser();
        verify(ticketPurchaseRepository).findByUserId(1L);
        verify(eventRepository).save(futureEvent);
        verify(eventRepository, never()).save(pastEvent);
        verify(userRepository).delete(user);
    }
}