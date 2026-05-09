package com.example.restaurantreservation.aggregator.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreOrderItemSnapshot {
    private Long menuItemId;
    private String name;
    private int quantity;
    private BigDecimal unitAmount;
    private BigDecimal lineTotal;
    private String currency;
    private String category;
}
