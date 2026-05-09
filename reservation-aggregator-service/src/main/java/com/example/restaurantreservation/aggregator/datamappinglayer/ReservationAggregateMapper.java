package com.example.restaurantreservation.aggregator.datamappinglayer;

import com.example.restaurantreservation.aggregator.domain.ReservationAggregate;
import com.example.restaurantreservation.aggregator.presentationlayer.dto.ReservationAggregateResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ReservationAggregateMapper {

    public ReservationAggregateResponseDTO toResponseDTO(ReservationAggregate aggregate) {
        return ReservationAggregateResponseDTO.builder()
                .aggregateId(aggregate.getAggregateId())
                .bookingId(aggregate.getBookingId())
                .preOrderId(aggregate.getPreOrderId())
                .customerId(aggregate.getCustomerId())
                .tableId(aggregate.getTableId())
                .reservationDate(aggregate.getReservationDate())
                .timeSlotStart(aggregate.getTimeSlotStart())
                .timeSlotEnd(aggregate.getTimeSlotEnd())
                .partySize(aggregate.getPartySize())
                .status(aggregate.getStatus())
                .totalAmount(aggregate.getTotalAmount())
                .currency(aggregate.getCurrency())
                .loyaltyPointsEarned(aggregate.getLoyaltyPointsEarned())
                .table(aggregate.getTable())
                .loyaltyAccount(aggregate.getLoyaltyAccount())
                .preOrderItems(aggregate.getPreOrderItems())
                .createdAt(aggregate.getCreatedAt())
                .updatedAt(aggregate.getUpdatedAt())
                .build();
    }
}
