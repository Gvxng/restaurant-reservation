package com.example.restaurantreservation.apigateway.domainclientlayer;

import com.example.restaurantreservation.apigateway.businesslogic.LoyaltyGatewayService;
import com.example.restaurantreservation.apigateway.presentation.dto.loyalty.CreateLoyaltyAccountRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.loyalty.LoyaltyAccountResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoyaltyGatewayServiceImpl implements LoyaltyGatewayService {

    private final RestTemplate restTemplate;

    @Value("${microservices.loyalty.base-url}")
    private String loyaltyBaseUrl;

    @Override
    public List<LoyaltyAccountResponseDTO> getAllLoyaltyAccounts() {
        return restTemplate.exchange(
                loyaltyBaseUrl + "/api/v1/loyalty-accounts",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<LoyaltyAccountResponseDTO>>() {
                }
        ).getBody();
    }

    @Override
    public LoyaltyAccountResponseDTO getLoyaltyAccountById(Long id) {
        return restTemplate.getForObject(
                loyaltyBaseUrl + "/api/v1/loyalty-accounts/" + id,
                LoyaltyAccountResponseDTO.class
        );
    }

    @Override
    public LoyaltyAccountResponseDTO createLoyaltyAccount(CreateLoyaltyAccountRequestDTO request) {
        return restTemplate.postForObject(
                loyaltyBaseUrl + "/api/v1/loyalty-accounts",
                request,
                LoyaltyAccountResponseDTO.class
        );
    }

    @Override
    public LoyaltyAccountResponseDTO updateLoyaltyAccount(Long id, CreateLoyaltyAccountRequestDTO request) {
        return restTemplate.exchange(
                loyaltyBaseUrl + "/api/v1/loyalty-accounts/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(request),
                LoyaltyAccountResponseDTO.class
        ).getBody();
    }

    @Override
    public void deleteLoyaltyAccount(Long id) {
        restTemplate.delete(loyaltyBaseUrl + "/api/v1/loyalty-accounts/" + id);
    }
}
