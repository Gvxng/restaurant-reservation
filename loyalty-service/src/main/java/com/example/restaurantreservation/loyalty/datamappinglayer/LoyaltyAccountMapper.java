package com.example.restaurantreservation.loyalty.datamappinglayer;

import com.example.restaurantreservation.loyalty.domain.LoyaltyAccount;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.LoyaltyAccountResponseDTO;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.LoyaltyAccountSummaryDTO;
import org.springframework.stereotype.Component;

@Component
public class LoyaltyAccountMapper {

    public LoyaltyAccountResponseDTO toResponseDTO(LoyaltyAccount account) {
        return LoyaltyAccountResponseDTO.builder()
                .accountId(account.getAccountId())
                .customerId(account.getCustomerId())
                .pointsBalance(account.getPointsBalance())
                .tier(account.getTier())
                .enrollmentDate(account.getEnrollmentDate())
                .build();
    }

    public LoyaltyAccountSummaryDTO toSummaryDTO(LoyaltyAccount account) {
        return LoyaltyAccountSummaryDTO.builder()
                .accountId(account.getAccountId())
                .customerId(account.getCustomerId())
                .pointsBalance(account.getPointsBalance())
                .tier(account.getTier())
                .build();
    }
}
