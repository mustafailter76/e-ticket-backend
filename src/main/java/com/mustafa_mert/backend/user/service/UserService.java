package com.mustafa_mert.backend.user.service;

import com.mustafa_mert.backend.user.dto.ChangePasswordRequest;
import com.mustafa_mert.backend.user.dto.UserResponse;

public interface UserService {

    UserResponse getMe();

    void changePassword(ChangePasswordRequest changePasswordRequest);

    void deleteMe();
}
