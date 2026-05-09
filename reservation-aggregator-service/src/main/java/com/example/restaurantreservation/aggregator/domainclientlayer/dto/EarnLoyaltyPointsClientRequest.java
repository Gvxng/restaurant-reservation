package com.example.restaurantreservation.aggregator.domainclientlayer.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EarnLoyaltyPointsClientRequest {
    private Long bookingId;
    private int points;
}
