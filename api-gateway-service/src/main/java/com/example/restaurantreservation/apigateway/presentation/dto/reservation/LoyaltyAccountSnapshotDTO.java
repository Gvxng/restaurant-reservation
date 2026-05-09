package com.example.restaurantreservation.apigateway.presentation.dto.reservation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoyaltyAccountSnapshotDTO {
    private Long accountId;
    private Long customerId;
    private int pointsBalance;
    private String tier;
}
