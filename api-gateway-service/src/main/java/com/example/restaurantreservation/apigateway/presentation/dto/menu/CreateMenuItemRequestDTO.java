package com.example.restaurantreservation.apigateway.presentation.dto.menu;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
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
