package com.example.restaurantreservation.aggregator.domainclientlayer;

import com.example.restaurantreservation.aggregator.domainclientlayer.dto.MenuItemClientResponse;

public interface MenuDomainClient {
    MenuItemClientResponse getMenuItemById(Long menuItemId);
}
