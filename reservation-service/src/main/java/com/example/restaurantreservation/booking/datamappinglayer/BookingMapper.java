package com.example.restaurantreservation.booking.datamappinglayer;

import com.example.restaurantreservation.booking.domain.OrderLineItem;
import com.example.restaurantreservation.booking.domain.PreOrder;
import com.example.restaurantreservation.booking.domain.TableBooking;
import com.example.restaurantreservation.booking.presentationlayer.dto.BookingResponseDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.OrderLineItemDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.PreOrderSummaryDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BookingMapper {

    public BookingResponseDTO toBaseResponseDTO(TableBooking booking) {
        return BookingResponseDTO.builder()
                .bookingId(booking.getBookingId())
                .customerId(booking.getCustomerId())
                .tableId(booking.getTableId())
                .preOrderId(booking.getPreOrderId())
                .reservationDate(booking.getReservationDate())
                .timeSlotStart(booking.getTimeSlotStart())
                .timeSlotEnd(booking.getTimeSlotEnd())
                .partySize(booking.getPartySize())
                .status(booking.getStatus())
                .loyaltyPointsEarned(booking.getLoyaltyPointsEarned())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    public PreOrderSummaryDTO toBasePreOrderSummaryDTO(PreOrder preOrder) {
        List<OrderLineItemDTO> itemDTOs = new ArrayList<>();
        for (OrderLineItem item : preOrder.getItems()) {
            OrderLineItemDTO dto = new OrderLineItemDTO();
            dto.setLineItemId(item.getLineItemId());
            dto.setMenuItemId(item.getMenuItemId());
            dto.setQuantity(item.getQuantity());
            dto.setUnitAmount(item.getUnitAmount());
            dto.setCurrency(item.getCurrency());
            itemDTOs.add(dto);
        }

        return PreOrderSummaryDTO.builder()
                .preOrderId(preOrder.getPreOrderId())
                .bookingId(preOrder.getBookingId())
                .totalAmount(preOrder.getTotalAmount())
                .currency(preOrder.getCurrency())
                .status(preOrder.getStatus())
                .submittedAt(preOrder.getSubmittedAt())
                .items(itemDTOs)
                .build();
    }
}
