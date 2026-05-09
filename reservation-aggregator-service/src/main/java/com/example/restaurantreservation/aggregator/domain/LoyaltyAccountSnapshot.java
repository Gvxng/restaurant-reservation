package com.example.restaurantreservation.aggregator.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyAccountSnapshot {
    private Long accountId;
    private Long customerId;
    private int pointsBalance;
    private String tier;
}
