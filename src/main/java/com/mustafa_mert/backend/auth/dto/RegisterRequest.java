package com.mustafa_mert.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @Email
    private String email;
    @NotBlank
    @Size(min = 2, max = 25)
    private String firstName;
    @NotBlank
    @Size(min = 2, max = 25)
    private String lastName;
    @NotBlank
    private String password;
}
