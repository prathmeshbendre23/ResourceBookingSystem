package com.example.resourcebooking.service.impl;

import com.example.resourcebooking.dto.request.ReservationRequest;
import com.example.resourcebooking.dto.request.ReservationUpdateRequest;
import com.example.resourcebooking.dto.response.PagedResponse;
import com.example.resourcebooking.dto.response.ReservationResponse;
import com.example.resourcebooking.dto.response.ResourceResponse;
import com.example.resourcebooking.dto.response.UserSummaryResponse;
import com.example.resourcebooking.entity.Reservation;
import com.example.resourcebooking.entity.ReservationStatus;
import com.example.resourcebooking.entity.Resource;
import com.example.resourcebooking.entity.Role;
import com.example.resourcebooking.entity.User;
import com.example.resourcebooking.exception.InvalidReservationException;
import com.example.resourcebooking.exception.ReservationConflictException;
import com.example.resourcebooking.exception.ReservationNotFoundException;
import com.example.resourcebooking.exception.ResourceNotFoundException;
import com.example.resourcebooking.exception.UserNotFoundException;
import com.example.resourcebooking.repository.ReservationRepository;
import com.example.resourcebooking.repository.ResourceRepository;
import com.example.resourcebooking.repository.UserRepository;
import com.example.resourcebooking.service.ReservationService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "startTime", "endTime", "price", "status", "createdAt"
    );

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, String currentUsername) {
        log.info("Creating reservation for user {} on resource ID {}", currentUsername, request.getResourceId());

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentUsername));

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException(request.getResourceId()));

        if (!Boolean.TRUE.equals(resource.getAvailable())) {
            throw new InvalidReservationException("Resource is currently marked as unavailable for booking");
        }

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();

        validateTimeRange(startTime, endTime);

        List<Reservation> conflicts = reservationRepository.findOverlappingReservations(
                resource.getId(), startTime, endTime, null
        );

        if (!conflicts.isEmpty()) {
            throw new ReservationConflictException(
                    "Resource '" + resource.getName() + "' is already reserved for the requested time interval"
            );
        }

        BigDecimal calculatedPrice = calculatePrice(resource.getPrice(), startTime, endTime);

        Reservation reservation = Reservation.builder()
                .resource(resource)
                .user(currentUser)
                .startTime(startTime)
                .endTime(endTime)
                .price(calculatedPrice)
                .status(ReservationStatus.CONFIRMED)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation created successfully with ID: {} for user: {}", saved.getId(), currentUsername);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReservationResponse> getReservations(
            String currentUsername,
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort) {

        log.debug("Querying reservations for user {} with page={}, size={}, sort={}", currentUsername, page, size, sort);

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentUsername));

        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Page size must not exceed 100");
        }
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("minPrice cannot be negative");
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("maxPrice cannot be negative");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }

        Sort sortOrder = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        Specification<Reservation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Non-admin users are strictly restricted to their own reservations
            if (!isAdmin) {
                predicates.add(cb.equal(root.get("user").get("id"), currentUser.getId()));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Reservation> reservationPage = reservationRepository.findAll(spec, pageable);
        Page<ReservationResponse> responsePage = reservationPage.map(this::mapToResponse);

        return PagedResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id, String currentUsername) {
        log.debug("Fetching reservation ID {} for user {}", id, currentUsername);

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentUsername));

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        validateOwnershipOrAdmin(reservation, currentUser, "view");

        return mapToResponse(reservation);
    }

    @Override
    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request, String currentUsername) {
        log.info("Updating reservation ID {} requested by {}", id, currentUsername);

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentUsername));

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        validateOwnershipOrAdmin(reservation, currentUser, "update");

        // Handle Status Update
        if (request.getStatus() != null) {
            if (currentUser.getRole() == Role.USER) {
                // USER can only cancel their own reservation
                if (request.getStatus() != ReservationStatus.CANCELLED) {
                    throw new AccessDeniedException("Regular users are only permitted to cancel their own reservations");
                }
            }
            reservation.setStatus(request.getStatus());
        }

        // Handle Time Update
        if (request.getStartTime() != null || request.getEndTime() != null) {
            if (currentUser.getRole() == Role.USER) {
                throw new AccessDeniedException("Regular users are only permitted to cancel their own reservations");
            }

            if (reservation.getStatus() == ReservationStatus.CANCELLED) {
                throw new InvalidReservationException("Cannot modify scheduled times of a cancelled reservation");
            }

            LocalDateTime newStart = request.getStartTime() != null ? request.getStartTime() : reservation.getStartTime();
            LocalDateTime newEnd = request.getEndTime() != null ? request.getEndTime() : reservation.getEndTime();

            validateTimeRange(newStart, newEnd);

            List<Reservation> conflicts = reservationRepository.findOverlappingReservations(
                    reservation.getResource().getId(), newStart, newEnd, reservation.getId()
            );

            if (!conflicts.isEmpty()) {
                throw new ReservationConflictException(
                        "Resource is already reserved for the requested time interval"
                );
            }

            BigDecimal newPrice = calculatePrice(reservation.getResource().getPrice(), newStart, newEnd);
            reservation.setStartTime(newStart);
            reservation.setEndTime(newEnd);
            reservation.setPrice(newPrice);
        }

        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation ID {} updated successfully", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteReservation(Long id, String currentUsername) {
        log.info("Deleting reservation ID {} requested by {}", id, currentUsername);

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentUsername));

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        validateOwnershipOrAdmin(reservation, currentUser, "delete");

        reservationRepository.delete(reservation);
        log.info("Reservation ID {} deleted successfully", id);
    }

    private void validateOwnershipOrAdmin(Reservation reservation, User currentUser, String action) {
        if (currentUser.getRole() != Role.ADMIN && !reservation.getUser().getId().equals(currentUser.getId())) {
            log.warn("IDOR attempt: User {} attempted to {} reservation ID {} owned by user ID {}",
                    currentUser.getUsername(), action, reservation.getId(), reservation.getUser().getId());
            throw new AccessDeniedException("Access denied: You are not authorized to " + action + " this reservation");
        }
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new InvalidReservationException("Start time and end time are required");
        }
        if (!startTime.isBefore(endTime)) {
            throw new InvalidReservationException("Start time must be before end time");
        }
        if (startTime.isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new InvalidReservationException("Reservation start time cannot be in the past");
        }
    }

    private BigDecimal calculatePrice(BigDecimal hourlyRate, LocalDateTime startTime, LocalDateTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes <= 0) {
            throw new InvalidReservationException("Reservation duration must be greater than zero");
        }
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal totalPrice = hourlyRate.multiply(hours).setScale(2, RoundingMode.HALF_UP);
        if (totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            totalPrice = hourlyRate;
        }
        return totalPrice;
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sort.split(",");
        String property = parts[0].trim();
        if (!ALLOWED_SORT_FIELDS.contains(property)) {
            throw new IllegalArgumentException(
                    "Invalid sort property: '" + property + "'. Allowed properties are: " + ALLOWED_SORT_FIELDS
            );
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
            direction = Sort.Direction.DESC;
        }

        return Sort.by(direction, property);
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        Resource resource = reservation.getResource();
        User user = reservation.getUser();

        ResourceResponse resourceResponse = ResourceResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .description(resource.getDescription())
                .type(resource.getType())
                .price(resource.getPrice())
                .available(resource.getAvailable())
                .createdAt(resource.getCreatedAt())
                .build();

        UserSummaryResponse userResponse = UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();

        return ReservationResponse.builder()
                .id(reservation.getId())
                .resource(resourceResponse)
                .user(userResponse)
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .price(reservation.getPrice())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
