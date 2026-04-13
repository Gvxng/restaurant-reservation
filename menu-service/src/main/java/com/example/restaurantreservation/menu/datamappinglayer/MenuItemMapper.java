package com.example.restaurantreservation.menu.datamappinglayer;

import com.example.restaurantreservation.menu.domain.MenuItem;
import com.example.restaurantreservation.menu.presentationlayer.dto.MenuItemResponseDTO;
import com.example.restaurantreservation.menu.presentationlayer.dto.MenuItemSummaryDTO;
import org.springframework.stereotype.Component;

@Component
public class MenuItemMapper {

    public MenuItemResponseDTO toResponseDTO(MenuItem item) {
        return MenuItemResponseDTO.builder()
                .menuItemId(item.getMenuItemId())
                .menuId(item.getMenu() != null ? item.getMenu().getMenuId() : null)
                .name(item.getName())
                .description(item.getDescription())
                .amount(item.getAmount())
                .currency(item.getCurrency())
                .category(item.getCategory())
                .isAvailable(item.isAvailable())
                .dietaryTags(item.getDietaryTags())
                .build();
    }

    public MenuItemSummaryDTO toSummaryDTO(MenuItem item) {
        return MenuItemSummaryDTO.builder()
                .menuItemId(item.getMenuItemId())
                .name(item.getName())
                .amount(item.getAmount())
                .currency(item.getCurrency())
                .isAvailable(item.isAvailable())
                .build();
    }
}
