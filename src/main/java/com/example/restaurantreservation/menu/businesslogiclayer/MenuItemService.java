package com.example.restaurantreservation.menu.businesslogiclayer;

import com.example.restaurantreservation.menu.presentationlayer.dto.CreateMenuItemRequestDTO;
import com.example.restaurantreservation.menu.presentationlayer.dto.MenuItemResponseDTO;
import com.example.restaurantreservation.menu.presentationlayer.dto.MenuItemSummaryDTO;

import java.util.List;


public interface MenuItemService {
    List<MenuItemResponseDTO> findAll();
    MenuItemResponseDTO findById(Long id);
    MenuItemSummaryDTO getSummary(Long id);
    MenuItemResponseDTO create(CreateMenuItemRequestDTO dto);
    MenuItemResponseDTO update(Long id, CreateMenuItemRequestDTO dto);
    void delete(Long id);
}
