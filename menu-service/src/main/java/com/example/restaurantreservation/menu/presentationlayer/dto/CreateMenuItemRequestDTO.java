package com.example.restaurantreservation.menu.presentationlayer.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.restaurantreservation.menu.domain.enums.MenuCategory;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class CreateMenuItemRequestDTO {

    private Long menuId;

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Price amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
    private String currency = "CAD";

    @NotNull(message = "Category is required")
    private MenuCategory category;

    @JsonProperty("available")
    @JsonAlias("isAvailable")
    private boolean isAvailable = true;

    private String dietaryTags;
}
