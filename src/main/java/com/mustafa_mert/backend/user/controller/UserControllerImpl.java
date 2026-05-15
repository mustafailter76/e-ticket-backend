package com.mustafa_mert.backend.user.controller;

import com.mustafa_mert.backend.common.response.RestBaseController;
import com.mustafa_mert.backend.common.response.RootEntity;
import com.mustafa_mert.backend.user.dto.ChangePasswordRequest;
import com.mustafa_mert.backend.user.dto.UserResponse;
import com.mustafa_mert.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserControllerImpl extends RestBaseController implements UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Override
    public ResponseEntity<RootEntity<UserResponse>> getMe() {
        return ok(userService.getMe());
    }

    @PatchMapping("/change-password")
    @Override
    public ResponseEntity<RootEntity<?>> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest);
        return ResponseEntity.ok(RootEntity.ok());
    }

    @DeleteMapping("/delete")
    @Override
    public ResponseEntity<RootEntity<?>> deleteMe() {
        userService.deleteMe();
        return ResponseEntity.ok(RootEntity.ok());
    }
}
