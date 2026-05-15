package com.mustafa_mert.backend.user.service;

import com.mustafa_mert.backend.common.exception.BaseException;
import com.mustafa_mert.backend.common.exception.ErrorMessage;
import com.mustafa_mert.backend.common.exception.MessageType;
import com.mustafa_mert.backend.event.entity.Event;
import com.mustafa_mert.backend.event.repository.EventRepository;
import com.mustafa_mert.backend.ticket_purchase.entity.TicketPurchase;
import com.mustafa_mert.backend.ticket_purchase.repository.TicketPurchaseRepository;
import com.mustafa_mert.backend.user.dto.ChangePasswordRequest;
import com.mustafa_mert.backend.user.dto.UserResponse;
import com.mustafa_mert.backend.user.entity.User;
import com.mustafa_mert.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketPurchaseRepository ticketPurchaseRepository;
    private final PasswordEncoder passwordEncoder;

    private User getCurrentUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(
                        new ErrorMessage(MessageType.USER_NOT_FOUND)
                ));
    }

    public UserResponse getMe() {

        User user = getCurrentUser();

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }

    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {

        User user = getCurrentUser();
        if (passwordEncoder.matches(changePasswordRequest.getPassword(), user.getPasswordHash())) {
            throw new BaseException(new ErrorMessage(MessageType.INVALID_CURRENT_PASSWORD));
        }

        user.setPasswordHash(passwordEncoder.encode(changePasswordRequest.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void deleteMe() {
        User user = getCurrentUser();
        if (user.getRole().equals("ADMIN")) {
            throw new BaseException(new ErrorMessage(MessageType.ONLY_FOR_USER));
        }
        List<TicketPurchase> ticketPurchases = ticketPurchaseRepository.findByUserId(user.getId());
        ticketPurchases.stream()
                .filter(tp -> tp.getEvent().getDateTime().isAfter(LocalDateTime.now()))
                .forEach(tp -> {
                    Event event = tp.getEvent();
                    event.setAvailableStock(event.getAvailableStock() + tp.getQuantity());
                    eventRepository.save(event);
                });
        userRepository.delete(user);
    }
}
