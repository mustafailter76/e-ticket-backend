package com.mustafa_mert.backend.auth.service;

import com.mustafa_mert.backend.auth.dto.AuthResponse;
import com.mustafa_mert.backend.auth.dto.LoginRequest;
import com.mustafa_mert.backend.auth.dto.RegisterRequest;
import com.mustafa_mert.backend.common.exception.BaseException;
import com.mustafa_mert.backend.common.exception.ErrorMessage;
import com.mustafa_mert.backend.common.exception.MessageType;
import com.mustafa_mert.backend.security.JwtService;
import com.mustafa_mert.backend.user.entity.User;
import com.mustafa_mert.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_ALREADY_EXIST, registerRequest.getEmail()));
        }

        User user = User.builder()
                .email(registerRequest.getEmail())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .role("USER")
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        String accessToken = jwtService.generateAccessToken(savedUser);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);

        return authResponse;
    }

    @Transactional
    @Override
    public AuthResponse login(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BaseException(
                        new ErrorMessage(MessageType.EMAIL_NOT_FOUND, loginRequest.getEmail())
                ));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new BaseException(new ErrorMessage(MessageType.PASSWORD_NOT_MATCH));
        }

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(jwtService.generateAccessToken(user));

        return authResponse;
    }
}
