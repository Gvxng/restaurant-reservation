package com.example.restaurantreservation.apigateway.presentation.dto.booking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
public class CreatePreOrderRequestDTO {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    private List<LineItemRequest> items;

    @Getter @Setter
    public static class LineItemRequest {
        @NotNull private Long menuItemId;
        @Min(1)  private int quantity;
        @NotNull private BigDecimal unitAmount;
        private String currency = "CAD";
    }
}
