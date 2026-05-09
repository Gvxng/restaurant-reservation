package com.example.restaurantreservation.aggregator.domainclientlayer.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class CreateBookingClientRequest {
    private Long customerId;
    private Long tableId;
    private LocalDate reservationDate;
    private LocalTime timeSlotStart;
    private LocalTime timeSlotEnd;
    private int partySize;
    private String status;
}
