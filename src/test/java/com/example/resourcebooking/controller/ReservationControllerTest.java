package com.example.resourcebooking.controller;

import com.example.resourcebooking.dto.request.ReservationRequest;
import com.example.resourcebooking.dto.request.ReservationUpdateRequest;
import com.example.resourcebooking.dto.response.PagedResponse;
import com.example.resourcebooking.dto.response.ReservationResponse;
import com.example.resourcebooking.dto.response.ResourceResponse;
import com.example.resourcebooking.dto.response.UserSummaryResponse;
import com.example.resourcebooking.entity.ReservationStatus;
import com.example.resourcebooking.exception.ReservationConflictException;
import com.example.resourcebooking.security.CustomAccessDeniedHandler;
import com.example.resourcebooking.security.CustomUserDetailsService;
import com.example.resourcebooking.security.JwtAuthenticationEntryPoint;
import com.example.resourcebooking.security.JwtAuthenticationFilter;
import com.example.resourcebooking.security.JwtService;
import com.example.resourcebooking.security.SecurityConfig;
import com.example.resourcebooking.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private ReservationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        ResourceResponse resourceResponse = ResourceResponse.builder()
                .id(1L)
                .name("Conference Room A")
                .type("Room")
                .price(new BigDecimal("50.00"))
                .available(true)
                .build();

        UserSummaryResponse userSummaryResponse = UserSummaryResponse.builder()
                .id(10L)
                .username("user")
                .email("user@example.com")
                .role("USER")
                .build();

        sampleResponse = ReservationResponse.builder()
                .id(100L)
                .resource(resourceResponse)
                .user(userSummaryResponse)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .price(new BigDecimal("100.00"))
                .status(ReservationStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("POST /api/reservations - USER creates reservation with identity extracted from JWT")
    void testCreateReservationSuccess() throws Exception {
        ReservationRequest request = ReservationRequest.builder()
                .resourceId(1L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .build();

        when(reservationService.createReservation(any(ReservationRequest.class), eq("user")))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.user.username").value("user"))
                .andExpect(jsonPath("$.price").value(100.00))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("POST /api/reservations - Conflict returns 409 CONFLICT")
    void testCreateReservationConflict() throws Exception {
        ReservationRequest request = ReservationRequest.builder()
                .resourceId(1L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .build();

        when(reservationService.createReservation(any(ReservationRequest.class), eq("user")))
                .thenThrow(new ReservationConflictException("Resource is already reserved for the requested time interval"));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Resource is already reserved for the requested time interval"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("GET /api/reservations - Returns paginated response")
    void testGetReservationsPaginated() throws Exception {
        PagedResponse<ReservationResponse> pagedResponse = PagedResponse.<ReservationResponse>builder()
                .content(List.of(sampleResponse))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .last(true)
                .build();

        when(reservationService.getReservations(eq("user"), any(), any(), any(), eq(0), eq(10), any()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/reservations?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("GET /api/reservations/200 - IDOR Protection: USER cannot access another user's reservation")
    void testIdorGetReservationDenied() throws Exception {
        when(reservationService.getReservationById(200L, "user"))
                .thenThrow(new AccessDeniedException("Access denied: You are not authorized to view this reservation"));

        mockMvc.perform(get("/api/reservations/200"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied: Access denied: You are not authorized to view this reservation"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("PUT /api/reservations/100 - USER cancels own reservation")
    void testUserCancelReservation() throws Exception {
        ReservationUpdateRequest updateRequest = ReservationUpdateRequest.builder()
                .status(ReservationStatus.CANCELLED)
                .build();

        sampleResponse.setStatus(ReservationStatus.CANCELLED);
        when(reservationService.updateReservation(eq(100L), any(ReservationUpdateRequest.class), eq("user")))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/reservations/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("DELETE /api/reservations/100 - Successfully deleted returns 204 NO_CONTENT")
    void testDeleteReservation() throws Exception {
        doNothing().when(reservationService).deleteReservation(100L, "user");

        mockMvc.perform(delete("/api/reservations/100"))
                .andExpect(status().isNoContent());
    }
}
