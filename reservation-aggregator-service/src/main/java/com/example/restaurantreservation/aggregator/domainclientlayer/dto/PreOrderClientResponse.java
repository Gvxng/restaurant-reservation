package com.example.restaurantreservation.aggregator.domainclientlayer.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PreOrderClientResponse {
    private Long preOrderId;
    private Long bookingId;
    private BigDecimal totalAmount;
    private String currency;
    private String status;
    private List<OrderLineItemClientResponse> items;
}
