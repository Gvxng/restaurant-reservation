package com.example.restaurantreservation.aggregator.businesslogiclayer;

import com.example.restaurantreservation.aggregator.presentationlayer.dto.CreateReservationRequestDTO;
import com.example.restaurantreservation.aggregator.presentationlayer.dto.ReservationAggregateResponseDTO;

import java.util.List;

public interface ReservationAggregatorService {
    List<ReservationAggregateResponseDTO> findAll();
    ReservationAggregateResponseDTO findById(String aggregateId);
    ReservationAggregateResponseDTO create(CreateReservationRequestDTO request);
    ReservationAggregateResponseDTO update(String aggregateId, CreateReservationRequestDTO request);
    void delete(String aggregateId);
}
