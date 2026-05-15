package com.mustafa_mert.backend.config;

import com.mustafa_mert.backend.user.entity.User;
import com.mustafa_mert.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@eticket.com")) {

            User admin = User.builder()
                    .email("admin@eticket.com")
                    .firstName("System")
                    .lastName("Admin")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .build();

            userRepository.save(admin);
        }

        if (!userRepository.existsByEmail("deneme@eticket.com")) {

            User user = User.builder()
                    .email("deneme@eticket.com")
                    .firstName("Mustafa")
                    .lastName("Mert")
                    .passwordHash(passwordEncoder.encode("deneme123"))
                    .role("USER")
                    .build();

            userRepository.save(user);
        }
    }
}