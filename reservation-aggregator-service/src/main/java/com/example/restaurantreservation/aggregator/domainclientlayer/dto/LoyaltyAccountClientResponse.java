package com.example.restaurantreservation.aggregator.domainclientlayer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoyaltyAccountClientResponse {
    private Long accountId;
    private Long customerId;
    private int pointsBalance;
    private String tier;
}
