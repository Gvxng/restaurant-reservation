package com.example.restaurantreservation.booking.datamappinglayer;

import com.example.restaurantreservation.booking.domain.OrderLineItem;
import com.example.restaurantreservation.booking.domain.PreOrder;
import com.example.restaurantreservation.booking.domain.TableBooking;
import com.example.restaurantreservation.booking.presentationlayer.dto.BookingResponseDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.OrderLineItemDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.PreOrderSummaryDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Component
public class BookingMapper {


    public BookingResponseDTO toBaseResponseDTO(TableBooking b) {
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self",         Map.of("href", "/api/v1/bookings/" + b.getBookingId()));
        links.put("all-bookings", Map.of("href", "/api/v1/bookings"));
        links.put("table",        Map.of("href", "/api/v1/dining-tables/" + b.getTableId()));
        links.put("loyalty",      Map.of("href", "/api/v1/loyalty-accounts/customer/" + b.getCustomerId()));
        if (b.getPreOrderId() != null) {
            links.put("pre-order", Map.of("href", "/api/v1/pre-orders/" + b.getPreOrderId()));
        }
        links.put("cancel", Map.of("href", "/api/v1/bookings/" + b.getBookingId(), "method", "DELETE"));

        return BookingResponseDTO.builder()
                .bookingId(b.getBookingId())
                .customerId(b.getCustomerId())
                .tableId(b.getTableId())
                .preOrderId(b.getPreOrderId())
                .reservationDate(b.getReservationDate())
                .timeSlotStart(b.getTimeSlotStart())
                .timeSlotEnd(b.getTimeSlotEnd())
                .partySize(b.getPartySize())
                .status(b.getStatus())
                .loyaltyPointsEarned(b.getLoyaltyPointsEarned())
                .createdAt(b.getCreatedAt())
                .table(null)
                .loyaltyAccount(null)
                .preOrder(null)
                ._links(links)
                .build();
    }


    public PreOrderSummaryDTO toBasePreOrderSummaryDTO(PreOrder po) {
        List<OrderLineItemDTO> itemDTOs = new ArrayList<>();
        for (OrderLineItem li : po.getItems()) {
            OrderLineItemDTO dto = new OrderLineItemDTO();
            dto.setLineItemId(li.getLineItemId());
            dto.setMenuItemId(li.getMenuItemId());
            dto.setQuantity(li.getQuantity());
            dto.setUnitAmount(li.getUnitAmount());
            dto.setCurrency(li.getCurrency());
            itemDTOs.add(dto);
        }

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self",    Map.of("href", "/api/v1/pre-orders/" + po.getPreOrderId()));
        links.put("booking", Map.of("href", "/api/v1/bookings/" + po.getBookingId()));

        return PreOrderSummaryDTO.builder()
                .preOrderId(po.getPreOrderId())
                .bookingId(po.getBookingId())
                .totalAmount(po.getTotalAmount())
                .currency(po.getCurrency())
                .status(po.getStatus())
                .submittedAt(po.getSubmittedAt())
                .items(itemDTOs)
                ._links(links)
                .build();
    }
}
