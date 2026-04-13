package com.example.restaurantreservation.menu.presentationlayer.dto;

import com.example.restaurantreservation.menu.domain.enums.MenuCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
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
    private boolean isAvailable;
    private String dietaryTags;
}
