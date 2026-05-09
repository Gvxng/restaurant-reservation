package com.example.restaurantreservation.aggregator.domainclientlayer;

import com.example.restaurantreservation.aggregator.domainclientlayer.dto.LoyaltyAccountClientResponse;

import java.util.Optional;

public interface LoyaltyDomainClient {
    Optional<LoyaltyAccountClientResponse> getLoyaltyAccountByCustomerId(Long customerId);
    Optional<LoyaltyAccountClientResponse> earnPoints(Long customerId, Long bookingId, int points);
}
