package com.example.restaurantreservation.apigateway.presentation.dto.reservation;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PreOrderItemSnapshotDTO {
    private Long menuItemId;
    private String name;
    private int quantity;
    private BigDecimal unitAmount;
    private BigDecimal lineTotal;
    private String currency;
    private String category;
}
