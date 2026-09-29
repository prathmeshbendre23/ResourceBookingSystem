package com.example.resourcebooking.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);

        userDetails = new User(
                "testuser",
                "encodedPassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    @DisplayName("Should generate valid token and extract subject")
    void testGenerateAndValidateToken() {
        String token = jwtService.generateToken(userDetails, "USER");

        assertNotNull(token);
        assertFalse(token.isBlank());

        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);

        String role = jwtService.extractRole(token);
        assertEquals("USER", role);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    @DisplayName("Should detect invalid user details for token")
    void testInvalidUserDetails() {
        String token = jwtService.generateToken(userDetails, "USER");

        UserDetails wrongUser = new User(
                "otheruser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        assertFalse(jwtService.isTokenValid(token, wrongUser));
    }

    @Test
    @DisplayName("Should detect expired token")
    void testExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", -1000L);
        String token = jwtService.generateToken(userDetails, "USER");

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }
}
