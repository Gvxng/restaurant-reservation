package com.example.restaurantreservation.aggregator.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "reservation_aggregates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationAggregate {

    @Id
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

    @Builder.Default
    private List<PreOrderItemSnapshot> preOrderItems = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
