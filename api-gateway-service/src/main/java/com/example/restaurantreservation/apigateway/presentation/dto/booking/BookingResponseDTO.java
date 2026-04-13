package com.example.restaurantreservation.apigateway.presentation.dto.booking;

import com.example.restaurantreservation.apigateway.presentation.dto.floor.DiningTableSummaryDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.loyalty.LoyaltyAccountSummaryDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;


@Getter @Setter
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

    // --- Aggregated from supporting subdomains ---
    private DiningTableSummaryDTO table;

    private LoyaltyAccountSummaryDTO loyaltyAccount;

    private PreOrderSummaryDTO preOrder;

    // HATEOAS
    @JsonProperty("_links")
    private Map<String, Object> _links;
}
