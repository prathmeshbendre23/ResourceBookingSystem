package com.example.resourcebooking.controller;

import com.example.resourcebooking.dto.request.ResourceRequest;
import com.example.resourcebooking.dto.response.ResourceResponse;
import com.example.resourcebooking.security.CustomAccessDeniedHandler;
import com.example.resourcebooking.security.CustomUserDetailsService;
import com.example.resourcebooking.security.JwtAuthenticationEntryPoint;
import com.example.resourcebooking.security.JwtAuthenticationFilter;
import com.example.resourcebooking.security.JwtService;
import com.example.resourcebooking.security.SecurityConfig;
import com.example.resourcebooking.service.ResourceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResourceController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResourceService resourceService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/resources without auth returns 401 UNAUTHORIZED")
    void testGetResourcesUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("GET /api/resources with USER role returns 200 OK")
    void testGetResourcesWithUserRole() throws Exception {
        ResourceResponse res = ResourceResponse.builder()
                .id(1L)
                .name("Room A")
                .type("Room")
                .price(new BigDecimal("50.00"))
                .available(true)
                .build();

        when(resourceService.getAllResources()).thenReturn(List.of(res));

        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Room A"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("POST /api/resources with USER role returns 403 FORBIDDEN (RBAC enforcement)")
    void testCreateResourceForbiddenForUser() throws Exception {
        ResourceRequest request = ResourceRequest.builder()
                .name("Unauthorized Room")
                .type("Room")
                .price(new BigDecimal("100.00"))
                .available(true)
                .build();

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("DELETE /api/resources/1 with USER role returns 403 FORBIDDEN")
    void testDeleteResourceForbiddenForUser() throws Exception {
        mockMvc.perform(delete("/api/resources/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /api/resources with ADMIN role returns 201 CREATED")
    void testCreateResourceAdminSuccess() throws Exception {
        ResourceRequest request = ResourceRequest.builder()
                .name("Boardroom X")
                .description("Luxury boardroom")
                .type("Room")
                .price(new BigDecimal("150.00"))
                .available(true)
                .build();

        ResourceResponse response = ResourceResponse.builder()
                .id(10L)
                .name("Boardroom X")
                .description("Luxury boardroom")
                .type("Room")
                .price(new BigDecimal("150.00"))
                .available(true)
                .build();

        when(resourceService.createResource(any(ResourceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Boardroom X"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("PUT /api/resources/10 with ADMIN role returns 200 OK")
    void testUpdateResourceAdminSuccess() throws Exception {
        ResourceRequest request = ResourceRequest.builder()
                .name("Boardroom X Updated")
                .type("Room")
                .price(new BigDecimal("175.00"))
                .available(true)
                .build();

        ResourceResponse response = ResourceResponse.builder()
                .id(10L)
                .name("Boardroom X Updated")
                .type("Room")
                .price(new BigDecimal("175.00"))
                .available(true)
                .build();

        when(resourceService.updateResource(eq(10L), any(ResourceRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/resources/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Boardroom X Updated"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("DELETE /api/resources/10 with ADMIN role returns 204 NO_CONTENT")
    void testDeleteResourceAdminSuccess() throws Exception {
        doNothing().when(resourceService).deleteResource(10L);

        mockMvc.perform(delete("/api/resources/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /api/resources with negative price returns 400 BAD_REQUEST")
    void testCreateResourceValidationFailure() throws Exception {
        ResourceRequest request = ResourceRequest.builder()
                .name("")
                .type("Room")
                .price(new BigDecimal("-10.00"))
                .available(true)
                .build();

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists());
    }
}
