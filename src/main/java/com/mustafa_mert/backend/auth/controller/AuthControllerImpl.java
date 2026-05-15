package com.mustafa_mert.backend.auth.controller;

import com.mustafa_mert.backend.auth.dto.AuthResponse;
import com.mustafa_mert.backend.auth.dto.LoginRequest;
import com.mustafa_mert.backend.auth.dto.RegisterRequest;
import com.mustafa_mert.backend.auth.service.AuthService;
import com.mustafa_mert.backend.common.response.RestBaseController;
import com.mustafa_mert.backend.common.response.RootEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/auth")
@RestController
@RequiredArgsConstructor
public class AuthControllerImpl extends RestBaseController implements AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Override
    public ResponseEntity<RootEntity<AuthResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    @Override
    public ResponseEntity<RootEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ok(authService.login(loginRequest));
    }
}
