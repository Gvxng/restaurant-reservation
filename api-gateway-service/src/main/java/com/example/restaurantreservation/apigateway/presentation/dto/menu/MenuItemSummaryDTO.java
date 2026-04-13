package com.example.restaurantreservation.apigateway.presentation.dto.menu;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemSummaryDTO {
    private Long menuItemId;
    private String name;
    private BigDecimal amount;
    private String currency;
    private boolean isAvailable;
}
