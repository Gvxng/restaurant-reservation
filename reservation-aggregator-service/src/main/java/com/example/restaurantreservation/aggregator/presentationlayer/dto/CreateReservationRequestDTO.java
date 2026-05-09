package com.example.restaurantreservation.aggregator.presentationlayer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class CreateReservationRequestDTO {

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

    private String status = "PENDING";

    @Valid
    @NotEmpty(message = "At least one pre-order item is required")
    private List<PreOrderItemRequestDTO> preOrderItems;
}
