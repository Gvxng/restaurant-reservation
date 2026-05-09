package com.example.restaurantreservation.apigateway.businesslogic;

import com.example.restaurantreservation.apigateway.presentation.dto.reservation.CreateReservationRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.reservation.ReservationAggregateResponseDTO;

import java.util.List;

public interface AggregatorGatewayService {
    List<ReservationAggregateResponseDTO> getAllReservations();
    ReservationAggregateResponseDTO getReservationById(String aggregateId);
    ReservationAggregateResponseDTO createReservation(CreateReservationRequestDTO request);
    ReservationAggregateResponseDTO updateReservation(String aggregateId, CreateReservationRequestDTO request);
    void deleteReservation(String aggregateId);
}
