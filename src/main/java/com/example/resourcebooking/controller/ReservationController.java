package com.example.resourcebooking.controller;

import com.example.resourcebooking.dto.request.ReservationRequest;
import com.example.resourcebooking.dto.request.ReservationUpdateRequest;
import com.example.resourcebooking.dto.response.ErrorResponse;
import com.example.resourcebooking.dto.response.PagedResponse;
import com.example.resourcebooking.dto.response.ReservationResponse;
import com.example.resourcebooking.entity.ReservationStatus;
import com.example.resourcebooking.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Endpoints for creating and managing resource reservations")
@SecurityRequirement(name = "BearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Create a reservation (User identity automatically extracted from JWT)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation created successfully",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation or invalid reservation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Reservation conflict / overlap",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        ReservationResponse response = reservationService.createReservation(request, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get reservations with optional filters, pagination, and sorting (USER sees own, ADMIN sees all)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of reservations",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter, pagination, or sorting parameter",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedResponse<ReservationResponse>> getReservations(
            @Parameter(description = "Filter by status (PENDING, CONFIRMED, CANCELLED)")
            @RequestParam(required = false) ReservationStatus status,

            @Parameter(description = "Filter by minimum price")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Filter by maximum price")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "Zero-based page index (default: 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (default: 10)")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sorting criteria in format: property(,asc|desc). Default: createdAt,desc")
            @RequestParam(required = false) String sort,

            Authentication authentication) {

        String currentUsername = authentication.getName();
        PagedResponse<ReservationResponse> response = reservationService.getReservations(
                currentUsername, status, minPrice, maxPrice, page, size, sort
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID (USER can access only own reservation, ADMIN can access any)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation details",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (Attempt to access another user's reservation)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable Long id,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        ReservationResponse response = reservationService.getReservationById(id, currentUsername);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update reservation (ADMIN can change status/times; USER can cancel own reservation)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation updated successfully",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation or invalid state update",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Reservation conflict / overlap on rescheduled time",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        ReservationResponse response = reservationService.updateReservation(id, request, currentUsername);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete reservation (ADMIN can delete any; USER can delete own)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reservation deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        reservationService.deleteReservation(id, currentUsername);
        return ResponseEntity.noContent().build();
    }
}
