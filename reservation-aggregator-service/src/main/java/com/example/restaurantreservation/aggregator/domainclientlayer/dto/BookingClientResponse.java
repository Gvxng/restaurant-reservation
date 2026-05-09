package com.example.restaurantreservation.aggregator.domainclientlayer.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
public class BookingClientResponse {
    private Long bookingId;
    private Long customerId;
    private Long tableId;
    private Long preOrderId;
    private LocalDate reservationDate;
    private LocalTime timeSlotStart;
    private LocalTime timeSlotEnd;
    private int partySize;
    private String status;
    private int loyaltyPointsEarned;
    private LocalDateTime createdAt;
    private DiningTableClientResponse table;
    private PreOrderClientResponse preOrder;
}
