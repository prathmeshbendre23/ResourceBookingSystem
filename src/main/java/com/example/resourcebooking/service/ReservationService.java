package com.example.resourcebooking.service;

import com.example.resourcebooking.dto.request.ReservationRequest;
import com.example.resourcebooking.dto.request.ReservationUpdateRequest;
import com.example.resourcebooking.dto.response.PagedResponse;
import com.example.resourcebooking.dto.response.ReservationResponse;
import com.example.resourcebooking.entity.ReservationStatus;

import java.math.BigDecimal;

public interface ReservationService {

    ReservationResponse createReservation(ReservationRequest request, String currentUsername);

    PagedResponse<ReservationResponse> getReservations(
            String currentUsername,
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort);

    ReservationResponse getReservationById(Long id, String currentUsername);

    ReservationResponse updateReservation(Long id, ReservationUpdateRequest request, String currentUsername);

    void deleteReservation(Long id, String currentUsername);
}
