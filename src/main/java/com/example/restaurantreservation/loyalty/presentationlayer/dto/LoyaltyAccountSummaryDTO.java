package com.example.restaurantreservation.loyalty.presentationlayer.dto;

import com.example.restaurantreservation.loyalty.domain.enums.LoyaltyTier;
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
