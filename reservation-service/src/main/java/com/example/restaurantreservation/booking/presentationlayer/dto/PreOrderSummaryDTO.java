package com.example.restaurantreservation.booking.presentationlayer.dto;

import com.example.restaurantreservation.booking.domain.enums.PreOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreOrderSummaryDTO {

    private Long preOrderId;
    private Long bookingId;
    private BigDecimal totalAmount;
    private String currency;
    private PreOrderStatus status;
    private LocalDateTime submittedAt;
    private List<OrderLineItemDTO> items;
}
