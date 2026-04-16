package com.example.restaurantreservation.apigateway.presentation.dto.menu;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemResponseDTO {

    private Long menuItemId;
    private Long menuId;
    private String name;
    private String description;
    private BigDecimal amount;
    private String currency;
    private MenuCategory category;
    @JsonProperty("available")
    @JsonAlias("isAvailable")
    private boolean isAvailable;
    private String dietaryTags;

    // R5 HATEOAS links
    @JsonProperty("_links")
    private Map<String, Object> _links;
}
