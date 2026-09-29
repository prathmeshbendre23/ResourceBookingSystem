package com.example.resourcebooking.repository;

import com.example.resourcebooking.entity.Role;
import com.example.resourcebooking.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("Should save user with BCrypt encrypted password and find by username")
    void testSaveAndFindUser() {
        String rawPassword = "SecurePassword@123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .username("testadmin")
                .email("testadmin@example.com")
                .password(encodedPassword)
                .role(Role.ADMIN)
                .build();

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotEquals(rawPassword, saved.getPassword());
        assertTrue(passwordEncoder.matches(rawPassword, saved.getPassword()));

        Optional<User> found = userRepository.findByUsername("testadmin");
        assertTrue(found.isPresent());
        assertEquals("testadmin@example.com", found.get().getEmail());
        assertEquals(Role.ADMIN, found.get().getRole());
    }

    @Test
    @DisplayName("Should check existence by username and email")
    void testExistsByUsernameAndEmail() {
        User user = User.builder()
                .username("uniqueuser")
                .email("unique@example.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        assertTrue(userRepository.existsByUsername("uniqueuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
        assertTrue(userRepository.existsByEmail("unique@example.com"));
        assertFalse(userRepository.existsByEmail("other@example.com"));
    }
}
