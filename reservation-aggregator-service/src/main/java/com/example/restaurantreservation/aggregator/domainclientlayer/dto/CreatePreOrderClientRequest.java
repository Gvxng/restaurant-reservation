package com.example.restaurantreservation.aggregator.domainclientlayer.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CreatePreOrderClientRequest {
    private Long bookingId;
    private List<LineItemRequest> items;

    @Getter
    @Builder
    public static class LineItemRequest {
        private Long menuItemId;
        private int quantity;
        private BigDecimal unitAmount;
        private String currency;
    }
}
