package com.example.restaurantreservation.apigateway.presentation.dto.loyalty;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyAccountSummaryDTO {
    private Long accountId;
    private Long customerId;
    private int pointsBalance;
    private LoyaltyTier tier;
}
