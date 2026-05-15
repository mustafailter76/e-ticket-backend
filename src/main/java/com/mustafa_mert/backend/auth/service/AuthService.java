package com.mustafa_mert.backend.auth.service;

import com.mustafa_mert.backend.auth.dto.AuthResponse;
import com.mustafa_mert.backend.auth.dto.LoginRequest;
import com.mustafa_mert.backend.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);
}
