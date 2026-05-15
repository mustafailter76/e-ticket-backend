package com.mustafa_mert.backend.auth.controller;

import com.mustafa_mert.backend.auth.dto.AuthResponse;
import com.mustafa_mert.backend.auth.dto.LoginRequest;
import com.mustafa_mert.backend.auth.dto.RegisterRequest;
import com.mustafa_mert.backend.common.response.RootEntity;
import org.springframework.http.ResponseEntity;

public interface AuthController {

    ResponseEntity<RootEntity<AuthResponse>> register(RegisterRequest registerRequest);

    ResponseEntity<RootEntity<AuthResponse>> login(LoginRequest loginRequest);
}
