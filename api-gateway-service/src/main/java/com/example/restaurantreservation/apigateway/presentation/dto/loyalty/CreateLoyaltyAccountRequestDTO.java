package com.example.restaurantreservation.apigateway.presentation.dto.loyalty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class CreateLoyaltyAccountRequestDTO {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    private int pointsBalance = 0;
    private LoyaltyTier tier = LoyaltyTier.BRONZE;
    @PastOrPresent
    private LocalDate enrollmentDate;
}
