package com.example.restaurantreservation.aggregator.domainclientlayer.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderLineItemClientResponse {
    private Long lineItemId;
    private Long menuItemId;
    private int quantity;
    private BigDecimal unitAmount;
    private BigDecimal lineTotal;
    private String currency;
}
