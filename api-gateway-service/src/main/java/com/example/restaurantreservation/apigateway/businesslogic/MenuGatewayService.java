package com.example.restaurantreservation.apigateway.businesslogic;

import com.example.restaurantreservation.apigateway.presentation.dto.menu.CreateMenuItemRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.menu.MenuItemResponseDTO;

import java.util.List;

public interface MenuGatewayService {

    List<MenuItemResponseDTO> getAllMenuItems();
    MenuItemResponseDTO getMenuItemById(Long id);
    MenuItemResponseDTO createMenuItem(CreateMenuItemRequestDTO request);
    MenuItemResponseDTO updateMenuItem(Long id, CreateMenuItemRequestDTO request);
    void deleteMenuItem(Long id);
}
