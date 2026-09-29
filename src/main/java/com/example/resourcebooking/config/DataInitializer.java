package com.example.resourcebooking.config;

import com.example.resourcebooking.entity.Resource;
import com.example.resourcebooking.entity.Role;
import com.example.resourcebooking.entity.User;
import com.example.resourcebooking.repository.ResourceRepository;
import com.example.resourcebooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedResources();
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Default ADMIN user seeded: username='admin'");
        }

        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("User@123"))
                    .role(Role.USER)
                    .build();
            userRepository.save(user);
            log.info("Default regular USER seeded: username='user'");
        }

        if (!userRepository.existsByUsername("user2")) {
            User user2 = User.builder()
                    .username("user2")
                    .email("user2@example.com")
                    .password(passwordEncoder.encode("User2@123"))
                    .role(Role.USER)
                    .build();
            userRepository.save(user2);
            log.info("Default regular USER2 seeded: username='user2'");
        }
    }

    private void seedResources() {
        if (resourceRepository.count() == 0) {
            List<Resource> sampleResources = List.of(
                    Resource.builder()
                            .name("Conference Room Alpha")
                            .description("Executive boardroom with 4K interactive display and video conferencing system")
                            .type("Room")
                            .price(new BigDecimal("75.00"))
                            .available(true)
                            .build(),
                    Resource.builder()
                            .name("Executive Shuttle Van")
                            .description("12-passenger luxury transport van with executive amenities")
                            .type("Vehicle")
                            .price(new BigDecimal("120.00"))
                            .available(true)
                            .build(),
                    Resource.builder()
                            .name("4K Cinema Projector")
                            .description("Ultra-HD 5000 lumens digital projector with wireless casting and soundbar")
                            .type("Equipment")
                            .price(new BigDecimal("35.00"))
                            .available(true)
                            .build(),
                    Resource.builder()
                            .name("Sound Recording Studio")
                            .description("Acoustically isolated studio suited for podcasts, voiceovers, and music production")
                            .type("Room")
                            .price(new BigDecimal("95.00"))
                            .available(true)
                            .build(),
                    Resource.builder()
                            .name("Industrial 3D Printer")
                            .description("Dual-extruder high-precision 3D printer for rapid prototyping")
                            .type("Equipment")
                            .price(new BigDecimal("45.00"))
                            .available(true)
                            .build()
            );

            resourceRepository.saveAll(sampleResources);
            log.info("Seeded {} sample resources", sampleResources.size());
        }
    }
}
