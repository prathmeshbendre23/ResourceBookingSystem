package com.example.resourcebooking.exception;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(String message) {
        super(message);
    }

    public ReservationNotFoundException(Long id) {
        super("Reservation not found with id: " + id);
    }
}
