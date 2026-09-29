package com.example.resourcebooking.dto.request;

import com.example.resourcebooking.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationUpdateRequest {

    private ReservationStatus status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
