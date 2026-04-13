package com.example.restaurantreservation.booking.presentationlayer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class OrderLineItemDTO {
    private Long lineItemId;
    private Long menuItemId;
    private String menuItemName;
    private int quantity;
    private BigDecimal unitAmount;
    private String currency;
}
