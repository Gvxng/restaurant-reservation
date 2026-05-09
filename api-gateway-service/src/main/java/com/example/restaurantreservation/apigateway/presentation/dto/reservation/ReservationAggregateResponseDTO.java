package com.example.restaurantreservation.apigateway.presentation.dto.reservation;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.util.Map;

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
    private DiningTableSnapshotDTO table;
    private LoyaltyAccountSnapshotDTO loyaltyAccount;
    private List<PreOrderItemSnapshotDTO> preOrderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonProperty("_links")
    private Map<String, Object> _links;
}
