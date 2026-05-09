package com.example.restaurantreservation.aggregator.domainclientlayer.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MenuItemClientResponse {
    private Long menuItemId;
    private Long menuId;
    private String name;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String category;

    @JsonProperty("available")
    @JsonAlias("isAvailable")
    private boolean available;

    private String dietaryTags;
}
