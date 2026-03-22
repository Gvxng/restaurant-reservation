package com.example.restaurantreservation.booking.presentationlayer.dto;

import com.example.restaurantreservation.booking.domain.enums.BookingStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
public class CreateBookingRequestDTO {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Table ID is required")
    private Long tableId;

    @NotNull(message = "Reservation date is required")
    @FutureOrPresent(message = "Reservation date cannot be in the past")
    private LocalDate reservationDate;

    @NotNull(message = "Time slot start is required")
    private LocalTime timeSlotStart;

    @NotNull(message = "Time slot end is required")
    private LocalTime timeSlotEnd;

    @Min(value = 1, message = "Party size must be at least 1")
    private int partySize;

    private BookingStatus status = BookingStatus.PENDING;
}
