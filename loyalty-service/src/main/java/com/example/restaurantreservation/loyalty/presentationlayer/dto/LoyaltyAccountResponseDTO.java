package com.example.restaurantreservation.loyalty.presentationlayer.dto;

import com.example.restaurantreservation.loyalty.domain.enums.LoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyAccountResponseDTO {

    private Long accountId;
    private Long customerId;
    private int pointsBalance;
    private LoyaltyTier tier;
    private LocalDate enrollmentDate;
}
