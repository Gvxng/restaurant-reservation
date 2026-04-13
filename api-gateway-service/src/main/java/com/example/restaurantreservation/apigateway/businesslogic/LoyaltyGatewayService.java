package com.example.restaurantreservation.apigateway.businesslogic;

import com.example.restaurantreservation.apigateway.presentation.dto.loyalty.CreateLoyaltyAccountRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.loyalty.LoyaltyAccountResponseDTO;

import java.util.List;

public interface LoyaltyGatewayService {

    List<LoyaltyAccountResponseDTO> getAllLoyaltyAccounts();
    LoyaltyAccountResponseDTO getLoyaltyAccountById(Long id);
    LoyaltyAccountResponseDTO createLoyaltyAccount(CreateLoyaltyAccountRequestDTO request);
    LoyaltyAccountResponseDTO updateLoyaltyAccount(Long id, CreateLoyaltyAccountRequestDTO request);
    void deleteLoyaltyAccount(Long id);
}
