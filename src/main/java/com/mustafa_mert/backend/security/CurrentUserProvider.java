package com.mustafa_mert.backend.security;

import com.mustafa_mert.backend.common.exception.BaseException;
import com.mustafa_mert.backend.common.exception.ErrorMessage;
import com.mustafa_mert.backend.common.exception.MessageType;
import com.mustafa_mert.backend.user.entity.User;
import com.mustafa_mert.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BaseException(
                    new ErrorMessage(MessageType.USER_NOT_FOUND)
            );
        }

        return authentication.getName();
    }

    public User getCurrentUser() {
        String email = getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(
                        new ErrorMessage(MessageType.USER_NOT_FOUND)
                ));
    }

    public boolean isCurrentUserAdmin() {
        User user = getCurrentUser();
        return user.getRole().equals("ADMIN");
    }

    public boolean isCurrentUserNormalUser() {
        User user = getCurrentUser();
        return user.getRole().equals("USER");
    }
}