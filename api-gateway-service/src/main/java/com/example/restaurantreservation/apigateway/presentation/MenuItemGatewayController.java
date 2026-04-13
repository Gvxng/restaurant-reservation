package com.example.restaurantreservation.apigateway.presentation;

import com.example.restaurantreservation.apigateway.businesslogic.MenuGatewayService;
import com.example.restaurantreservation.apigateway.presentation.dto.menu.CreateMenuItemRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.menu.MenuItemResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/menu-items")
@RequiredArgsConstructor
public class MenuItemGatewayController {

    private final MenuGatewayService menuGatewayService;

    @GetMapping
    public ResponseEntity<List<MenuItemResponseDTO>> getAll() {
        List<MenuItemResponseDTO> menuItems = menuGatewayService.getAllMenuItems();
        menuItems.forEach(this::addLinks);
        return ResponseEntity.ok(menuItems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addLinks(menuGatewayService.getMenuItemById(id)));
    }

    @PostMapping
    public ResponseEntity<MenuItemResponseDTO> create(@RequestBody @Valid CreateMenuItemRequestDTO request) {
        MenuItemResponseDTO created = addLinks(menuGatewayService.createMenuItem(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getMenuItemId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreateMenuItemRequestDTO request) {
        return ResponseEntity.ok(addLinks(menuGatewayService.updateMenuItem(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuGatewayService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    private MenuItemResponseDTO addLinks(MenuItemResponseDTO dto) {
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", link(linkTo(methodOn(MenuItemGatewayController.class).getById(dto.getMenuItemId())).toUri().toString()));
        links.put("all-items", link(linkTo(methodOn(MenuItemGatewayController.class).getAll()).toUri().toString()));
        dto.set_links(links);
        return dto;
    }

    private Map<String, Object> link(String href) {
        return Map.of("href", href);
    }
}
