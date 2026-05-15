package com.mustafa_mert.backend.auth;

import com.mustafa_mert.backend.auth.dto.AuthResponse;
import com.mustafa_mert.backend.auth.dto.LoginRequest;
import com.mustafa_mert.backend.auth.dto.RegisterRequest;
import com.mustafa_mert.backend.auth.service.AuthServiceImpl;
import com.mustafa_mert.backend.common.exception.BaseException;
import com.mustafa_mert.backend.security.JwtService;
import com.mustafa_mert.backend.user.entity.User;
import com.mustafa_mert.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@gmail.com");
        registerRequest.setFirstName("Mustafa");
        registerRequest.setLastName("Ilter");
        registerRequest.setPassword("12345");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("12345");

        user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .firstName("Mustafa")
                .lastName("Ilter")
                .role("USER")
                .passwordHash("encodedPassword")
                .build();
    }

    @Test
    void register_WhenEmailDoesNotExist_ShouldReturnAuthResponse() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());

        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateAccessToken(user);
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldThrowBaseException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(BaseException.class, () -> authService.register(registerRequest));

        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateAccessToken(any(User.class));
    }

    @Test
    void login_WhenEmailAndPasswordAreCorrect_ShouldReturnAuthResponse() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), user.getPasswordHash());
        verify(jwtService, times(1)).generateAccessToken(user);
    }

    @Test
    void login_WhenEmailNotFound_ShouldThrowBaseException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> authService.login(loginRequest));

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateAccessToken(any(User.class));
    }

    @Test
    void login_WhenPasswordDoesNotMatch_ShouldThrowBaseException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())).thenReturn(false);

        assertThrows(BaseException.class, () -> authService.login(loginRequest));

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), user.getPasswordHash());
        verify(jwtService, never()).generateAccessToken(any(User.class));
    }
}