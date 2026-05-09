package com.example.restaurantreservation.aggregator.presentationlayer.dto;

import com.example.restaurantreservation.aggregator.domain.DiningTableSnapshot;
import com.example.restaurantreservation.aggregator.domain.LoyaltyAccountSnapshot;
import com.example.restaurantreservation.aggregator.domain.PreOrderItemSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationAggregateResponseDTO {
    private String aggregateId;
    private Long bookingId;
    private Long preOrderId;
    private Long customerId;
    private Long tableId;
    private LocalDate reservationDate;
    private LocalTime timeSlotStart;
    private LocalTime timeSlotEnd;
    private int partySize;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private Integer loyaltyPointsEarned;
    private DiningTableSnapshot table;
    private LoyaltyAccountSnapshot loyaltyAccount;
    private List<PreOrderItemSnapshot> preOrderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
