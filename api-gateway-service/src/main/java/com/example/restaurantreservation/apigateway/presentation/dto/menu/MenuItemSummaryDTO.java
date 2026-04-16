package com.example.restaurantreservation.apigateway.presentation.dto.menu;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("available")
    @JsonAlias("isAvailable")
    private boolean isAvailable;
}
