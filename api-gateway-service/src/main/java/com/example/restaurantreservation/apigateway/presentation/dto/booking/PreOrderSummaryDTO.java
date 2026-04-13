package com.example.restaurantreservation.apigateway.presentation.dto.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter @Setter
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

    @JsonProperty("_links")
    private Map<String, Object> _links;
}
