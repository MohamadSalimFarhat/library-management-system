package com.maids.lms.config;

import com.maids.lms.entity.User;
import com.maids.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Default admin user created: username=admin, password=admin123");
        }

        if (!userRepository.existsByUsername("librarian")) {
            User librarian = User.builder()
                    .username("librarian")
                    .password(passwordEncoder.encode("lib123"))
                    .role(User.Role.LIBRARIAN)
                    .build();
            userRepository.save(librarian);
            log.info("Default librarian user created: username=librarian, password=lib123");
        }
    }
}
