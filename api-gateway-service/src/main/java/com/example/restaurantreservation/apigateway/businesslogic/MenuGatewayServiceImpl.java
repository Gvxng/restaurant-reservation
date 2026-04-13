package com.example.restaurantreservation.apigateway.businesslogic;

import com.example.restaurantreservation.apigateway.presentation.dto.menu.CreateMenuItemRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.menu.MenuItemResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuGatewayServiceImpl implements MenuGatewayService {

    private final RestTemplate restTemplate;

    @Value("${microservices.menu.base-url}")
    private String menuBaseUrl;

    @Override
    public List<MenuItemResponseDTO> getAllMenuItems() {
        return restTemplate.exchange(
                menuBaseUrl + "/api/v1/menu-items",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MenuItemResponseDTO>>() {
                }
        ).getBody();
    }

    @Override
    public MenuItemResponseDTO getMenuItemById(Long id) {
        return restTemplate.getForObject(menuBaseUrl + "/api/v1/menu-items/" + id, MenuItemResponseDTO.class);
    }

    @Override
    public MenuItemResponseDTO createMenuItem(CreateMenuItemRequestDTO request) {
        return restTemplate.postForObject(menuBaseUrl + "/api/v1/menu-items", request, MenuItemResponseDTO.class);
    }

    @Override
    public MenuItemResponseDTO updateMenuItem(Long id, CreateMenuItemRequestDTO request) {
        return restTemplate.exchange(
                menuBaseUrl + "/api/v1/menu-items/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(request),
                MenuItemResponseDTO.class
        ).getBody();
    }

    @Override
    public void deleteMenuItem(Long id) {
        restTemplate.delete(menuBaseUrl + "/api/v1/menu-items/" + id);
    }
}
