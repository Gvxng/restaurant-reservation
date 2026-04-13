package com.example.restaurantreservation.booking.presentationlayer.dto;

import com.example.restaurantreservation.booking.domain.enums.BookingStatus;
import com.example.restaurantreservation.floor.presentationlayer.dto.DiningTableSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {

    private Long bookingId;
    private Long customerId;
    private Long tableId;
    private Long preOrderId;
    private LocalDate reservationDate;
    private LocalTime timeSlotStart;
    private LocalTime timeSlotEnd;
    private int partySize;
    private BookingStatus status;
    private int loyaltyPointsEarned;
    private LocalDateTime createdAt;
    private DiningTableSummaryDTO table;
    private PreOrderSummaryDTO preOrder;
}
