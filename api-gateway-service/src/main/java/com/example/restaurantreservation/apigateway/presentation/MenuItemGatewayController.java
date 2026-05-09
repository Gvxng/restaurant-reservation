package com.example.restaurantreservation.apigateway.presentation;

import com.example.restaurantreservation.apigateway.businesslogic.MenuGatewayService;
import com.example.restaurantreservation.apigateway.presentation.dto.menu.CreateMenuItemRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.menu.MenuItemResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Menu Items", description = "Restaurant menu item endpoints")
public class MenuItemGatewayController {

    private final MenuGatewayService menuGatewayService;

    @GetMapping
    @Operation(summary = "Get all menu items", description = "Returns every menu item available through the API gateway.")
    @ApiResponse(responseCode = "200", description = "Menu items returned successfully")
    public ResponseEntity<List<MenuItemResponseDTO>> getAll() {
        List<MenuItemResponseDTO> menuItems = menuGatewayService.getAllMenuItems();
        menuItems.forEach(this::addLinks);
        return ResponseEntity.ok(menuItems);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a menu item by ID", description = "Returns one menu item by its database ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu item returned successfully"),
            @ApiResponse(responseCode = "404", description = "Menu item not found")
    })
    public ResponseEntity<MenuItemResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addLinks(menuGatewayService.getMenuItemById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a menu item", description = "Creates a menu item in the menu service through the API gateway.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Menu item created successfully"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation"),
            @ApiResponse(responseCode = "409", description = "Menu item business rule violation")
    })
    public ResponseEntity<MenuItemResponseDTO> create(@RequestBody @Valid CreateMenuItemRequestDTO request) {
        MenuItemResponseDTO created = addLinks(menuGatewayService.createMenuItem(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getMenuItemId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a menu item", description = "Updates an existing menu item by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation"),
            @ApiResponse(responseCode = "404", description = "Menu item not found"),
            @ApiResponse(responseCode = "409", description = "Menu item business rule violation")
    })
    public ResponseEntity<MenuItemResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreateMenuItemRequestDTO request) {
        return ResponseEntity.ok(addLinks(menuGatewayService.updateMenuItem(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a menu item", description = "Deletes a menu item by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Menu item deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Menu item not found")
    })
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
