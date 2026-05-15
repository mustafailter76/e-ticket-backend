package com.mustafa_mert.backend.user.controller;

import com.mustafa_mert.backend.common.response.RootEntity;
import com.mustafa_mert.backend.user.dto.ChangePasswordRequest;
import com.mustafa_mert.backend.user.dto.UserResponse;
import org.springframework.http.ResponseEntity;

public interface UserController {

    ResponseEntity<RootEntity<UserResponse>> getMe();

    ResponseEntity<RootEntity<?>> changePassword(ChangePasswordRequest changePasswordRequest);

    ResponseEntity<RootEntity<?>> deleteMe();
}
