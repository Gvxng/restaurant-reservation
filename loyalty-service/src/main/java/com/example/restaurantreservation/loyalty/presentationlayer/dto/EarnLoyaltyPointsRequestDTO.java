package com.example.restaurantreservation.loyalty.presentationlayer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EarnLoyaltyPointsRequestDTO {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @Min(value = 1, message = "Points must be at least 1")
    private int points;
}
