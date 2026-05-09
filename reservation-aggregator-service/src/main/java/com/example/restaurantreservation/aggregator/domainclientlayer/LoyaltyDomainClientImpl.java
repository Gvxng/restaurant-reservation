package com.example.restaurantreservation.aggregator.domainclientlayer;

import com.example.restaurantreservation.aggregator.domainclientlayer.dto.EarnLoyaltyPointsClientRequest;
import com.example.restaurantreservation.aggregator.domainclientlayer.dto.LoyaltyAccountClientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoyaltyDomainClientImpl extends DomainClientSupport implements LoyaltyDomainClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.loyalty.base-url}")
    private String loyaltyBaseUrl;

    @Override
    public Optional<LoyaltyAccountClientResponse> getLoyaltyAccountByCustomerId(Long customerId) {
        try {
            LoyaltyAccountClientResponse response = webClient().get()
                    .uri("/api/v1/loyalty-accounts/customer/{customerId}", customerId)
                    .retrieve()
                    .bodyToMono(LoyaltyAccountClientResponse.class)
                    .block();
            return Optional.ofNullable(response);
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw mapResponseException("loyalty-service", ex);
        } catch (WebClientRequestException ex) {
            throw mapRequestException("loyalty-service", ex);
        }
    }

    @Override
    public Optional<LoyaltyAccountClientResponse> earnPoints(Long customerId, Long bookingId, int points) {
        try {
            LoyaltyAccountClientResponse response = webClient().post()
                    .uri("/api/v1/loyalty-accounts/customer/{customerId}/points", customerId)
                    .bodyValue(EarnLoyaltyPointsClientRequest.builder()
                            .bookingId(bookingId)
                            .points(points)
                            .build())
                    .retrieve()
                    .bodyToMono(LoyaltyAccountClientResponse.class)
                    .block();
            return Optional.ofNullable(response);
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return Optional.empty();
            }
            throw mapResponseException("loyalty-service", ex);
        } catch (WebClientRequestException ex) {
            throw mapRequestException("loyalty-service", ex);
        }
    }

    private WebClient webClient() {
        return webClientBuilder.baseUrl(loyaltyBaseUrl).build();
    }
}
