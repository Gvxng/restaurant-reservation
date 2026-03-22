package com.example.restaurantreservation.loyalty.datamappinglayer;

import com.example.restaurantreservation.loyalty.domain.LoyaltyAccount;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.LoyaltyAccountResponseDTO;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.LoyaltyAccountSummaryDTO;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;


@Component
public class LoyaltyAccountMapper {

    public LoyaltyAccountResponseDTO toResponseDTO(LoyaltyAccount a) {
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self",         Map.of("href", "/api/v1/loyalty-accounts/" + a.getAccountId()));
        links.put("all-accounts", Map.of("href", "/api/v1/loyalty-accounts"));

        return LoyaltyAccountResponseDTO.builder()
                .accountId(a.getAccountId())
                .customerId(a.getCustomerId())
                .pointsBalance(a.getPointsBalance())
                .tier(a.getTier())
                .enrollmentDate(a.getEnrollmentDate())
                ._links(links)
                .build();
    }

    public LoyaltyAccountSummaryDTO toSummaryDTO(LoyaltyAccount a) {
        return LoyaltyAccountSummaryDTO.builder()
                .accountId(a.getAccountId())
                .customerId(a.getCustomerId())
                .pointsBalance(a.getPointsBalance())
                .tier(a.getTier())
                .build();
    }
}
