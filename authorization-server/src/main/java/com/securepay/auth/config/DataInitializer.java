package com.securepay.auth.config;

import com.securepay.auth.entity.User;
import com.securepay.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .customerId("CUS100")
                    .roles("ROLE_ADMIN,ROLE_CUSTOMER")
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(admin);
            
            User customer = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .customerId("CUS200")
                    .roles("ROLE_CUSTOMER")
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(customer);
        }
    }
}