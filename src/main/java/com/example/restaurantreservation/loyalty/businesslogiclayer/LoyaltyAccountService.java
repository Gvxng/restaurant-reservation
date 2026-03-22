package com.example.restaurantreservation.loyalty.businesslogiclayer;

import com.example.restaurantreservation.loyalty.presentationlayer.dto.CreateLoyaltyAccountRequestDTO;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.LoyaltyAccountResponseDTO;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.LoyaltyAccountSummaryDTO;

import java.util.List;


public interface LoyaltyAccountService {
    List<LoyaltyAccountResponseDTO> findAll();
    LoyaltyAccountResponseDTO findById(Long id);
    LoyaltyAccountSummaryDTO getSummaryByCustomerId(Long customerId);
    LoyaltyAccountResponseDTO create(CreateLoyaltyAccountRequestDTO dto);
    LoyaltyAccountResponseDTO update(Long id, CreateLoyaltyAccountRequestDTO dto);
    void delete(Long id);


    int earnPoints(Long customerId, Long bookingId, int points);
}
