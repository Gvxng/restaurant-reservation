package com.example.restaurantreservation.apigateway.businesslogic;

import com.example.restaurantreservation.apigateway.presentation.dto.reservation.CreateReservationRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.reservation.ReservationAggregateResponseDTO;
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
public class AggregatorGatewayServiceImpl implements AggregatorGatewayService {

    private final RestTemplate restTemplate;

    @Value("${microservices.aggregator.base-url}")
    private String aggregatorBaseUrl;

    @Override
    public List<ReservationAggregateResponseDTO> getAllReservations() {
        return restTemplate.exchange(
                aggregatorBaseUrl + "/api/v1/reservations",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ReservationAggregateResponseDTO>>() {
                }
        ).getBody();
    }

    @Override
    public ReservationAggregateResponseDTO getReservationById(String aggregateId) {
        return restTemplate.getForObject(
                aggregatorBaseUrl + "/api/v1/reservations/" + aggregateId,
                ReservationAggregateResponseDTO.class
        );
    }

    @Override
    public ReservationAggregateResponseDTO createReservation(CreateReservationRequestDTO request) {
        return restTemplate.postForObject(
                aggregatorBaseUrl + "/api/v1/reservations",
                request,
                ReservationAggregateResponseDTO.class
        );
    }

    @Override
    public ReservationAggregateResponseDTO updateReservation(String aggregateId, CreateReservationRequestDTO request) {
        return restTemplate.exchange(
                aggregatorBaseUrl + "/api/v1/reservations/" + aggregateId,
                HttpMethod.PUT,
                new HttpEntity<>(request),
                ReservationAggregateResponseDTO.class
        ).getBody();
    }

    @Override
    public void deleteReservation(String aggregateId) {
        restTemplate.delete(aggregatorBaseUrl + "/api/v1/reservations/" + aggregateId);
    }
}
