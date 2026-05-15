package com.mustafa_mert.backend.security;

import com.mustafa_mert.backend.common.exception.MessageType;
import com.mustafa_mert.backend.common.response.RootEntity;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthEntryPoint implements AuthenticationEntryPoint {

    // This class is responsible for handling unauthorized access attempts.
    // When a user tries to access a protected resource without proper authentication, this entry point will be triggered.
    // It constructs a JSON response with an appropriate error message and status code, indicating that the user is unauthorized.

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        RootEntity<?> body = RootEntity.error(
                MessageType.UNAUTHORIZED.getMessage(),
                MessageType.UNAUTHORIZED.getStatus()
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}