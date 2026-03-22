package com.example.restaurantreservation.loyalty.presentationlayer.dto;

import com.example.restaurantreservation.loyalty.domain.enums.LoyaltyTier;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyAccountResponseDTO {
    private Long accountId;
    private Long customerId;
    private int pointsBalance;
    private LoyaltyTier tier;
    private LocalDate enrollmentDate;

    @JsonProperty("_links")
    private Map<String, Object> _links;
}
