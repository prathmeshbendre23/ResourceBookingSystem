package com.example.resourcebooking.service;

import com.example.resourcebooking.dto.request.ReservationRequest;
import com.example.resourcebooking.dto.request.ReservationUpdateRequest;
import com.example.resourcebooking.dto.response.ReservationResponse;
import com.example.resourcebooking.entity.Reservation;
import com.example.resourcebooking.entity.ReservationStatus;
import com.example.resourcebooking.entity.Resource;
import com.example.resourcebooking.entity.Role;
import com.example.resourcebooking.entity.User;
import com.example.resourcebooking.exception.InvalidReservationException;
import com.example.resourcebooking.exception.ReservationConflictException;
import com.example.resourcebooking.repository.ReservationRepository;
import com.example.resourcebooking.repository.ResourceRepository;
import com.example.resourcebooking.repository.UserRepository;
import com.example.resourcebooking.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private User regularUser;
    private User otherUser;
    private User adminUser;
    private Resource sampleResource;

    @BeforeEach
    void setUp() {
        regularUser = User.builder()
                .id(1L)
                .username("user")
                .email("user@example.com")
                .role(Role.USER)
                .build();

        otherUser = User.builder()
                .id(2L)
                .username("other")
                .email("other@example.com")
                .role(Role.USER)
                .build();

        adminUser = User.builder()
                .id(99L)
                .username("admin")
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        sampleResource = Resource.builder()
                .id(10L)
                .name("Conference Room Alpha")
                .type("Room")
                .price(new BigDecimal("50.00"))
                .available(true)
                .build();
    }

    @Test
    @DisplayName("Should successfully create reservation and calculate price correctly with BigDecimal")
    void testCreateReservationSuccess() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        ReservationRequest request = ReservationRequest.builder()
                .resourceId(sampleResource.getId())
                .startTime(start)
                .endTime(end)
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        when(resourceRepository.findById(sampleResource.getId())).thenReturn(Optional.of(sampleResource));
        when(reservationRepository.findOverlappingReservations(eq(sampleResource.getId()), eq(start), eq(end), isNull()))
                .thenReturn(Collections.emptyList());

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        ReservationResponse response = reservationService.createReservation(request, "user");

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(sampleResource.getId(), response.getResource().getId());
        assertEquals(regularUser.getId(), response.getUser().getId());
        // 2 hours * $50.00 = $100.00
        assertEquals(new BigDecimal("100.00"), response.getPrice());
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());

        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should detect reservation conflict/overlap and throw ReservationConflictException")
    void testCreateReservationConflict() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(2);

        ReservationRequest request = ReservationRequest.builder()
                .resourceId(sampleResource.getId())
                .startTime(start)
                .endTime(end)
                .build();

        Reservation existingReservation = Reservation.builder()
                .id(50L)
                .resource(sampleResource)
                .startTime(start.plusMinutes(30))
                .endTime(end.plusMinutes(30))
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        when(resourceRepository.findById(sampleResource.getId())).thenReturn(Optional.of(sampleResource));
        when(reservationRepository.findOverlappingReservations(eq(sampleResource.getId()), eq(start), eq(end), isNull()))
                .thenReturn(List.of(existingReservation));

        assertThrows(ReservationConflictException.class, () ->
                reservationService.createReservation(request, "user")
        );

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidReservationException when startTime is after endTime")
    void testInvalidTimeRange() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        LocalDateTime end = start.minusHours(1);

        ReservationRequest request = ReservationRequest.builder()
                .resourceId(sampleResource.getId())
                .startTime(start)
                .endTime(end)
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        when(resourceRepository.findById(sampleResource.getId())).thenReturn(Optional.of(sampleResource));

        assertThrows(InvalidReservationException.class, () ->
                reservationService.createReservation(request, "user")
        );
    }

    @Test
    @DisplayName("IDOR Protection: USER cannot view another user's reservation")
    void testIdorViewProtection() {
        Reservation otherReservation = Reservation.builder()
                .id(200L)
                .resource(sampleResource)
                .user(otherUser) // Owned by otherUser (id=2)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .price(new BigDecimal("50.00"))
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser)); // regularUser (id=1)
        when(reservationRepository.findById(200L)).thenReturn(Optional.of(otherReservation));

        assertThrows(AccessDeniedException.class, () ->
                reservationService.getReservationById(200L, "user")
        );
    }

    @Test
    @DisplayName("ADMIN can view any user's reservation")
    void testAdminCanViewAnyReservation() {
        Reservation otherReservation = Reservation.builder()
                .id(200L)
                .resource(sampleResource)
                .user(otherUser)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .price(new BigDecimal("50.00"))
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(reservationRepository.findById(200L)).thenReturn(Optional.of(otherReservation));

        ReservationResponse response = reservationService.getReservationById(200L, "admin");
        assertNotNull(response);
        assertEquals(200L, response.getId());
        assertEquals("other", response.getUser().getUsername());
    }

    @Test
    @DisplayName("USER can cancel own reservation")
    void testUserCanCancelOwnReservation() {
        Reservation myReservation = Reservation.builder()
                .id(300L)
                .resource(sampleResource)
                .user(regularUser)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(2))
                .price(new BigDecimal("100.00"))
                .status(ReservationStatus.CONFIRMED)
                .build();

        ReservationUpdateRequest updateRequest = ReservationUpdateRequest.builder()
                .status(ReservationStatus.CANCELLED)
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        when(reservationRepository.findById(300L)).thenReturn(Optional.of(myReservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse response = reservationService.updateReservation(300L, updateRequest, "user");
        assertEquals(ReservationStatus.CANCELLED, response.getStatus());
    }

    @Test
    @DisplayName("USER cannot arbitrarily confirm or modify another user's reservation")
    void testUserCannotConfirmOrModifyOtherReservation() {
        Reservation otherReservation = Reservation.builder()
                .id(400L)
                .resource(sampleResource)
                .user(otherUser)
                .status(ReservationStatus.PENDING)
                .build();

        ReservationUpdateRequest updateRequest = ReservationUpdateRequest.builder()
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        when(reservationRepository.findById(400L)).thenReturn(Optional.of(otherReservation));

        assertThrows(AccessDeniedException.class, () ->
                reservationService.updateReservation(400L, updateRequest, "user")
        );
    }
}
