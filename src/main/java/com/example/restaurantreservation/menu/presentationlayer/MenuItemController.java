package com.example.restaurantreservation.menu.presentationlayer;

import com.example.restaurantreservation.menu.businesslogiclayer.MenuItemService;
import com.example.restaurantreservation.menu.businesslogiclayer.MenuItemServiceImpl;
import com.example.restaurantreservation.menu.presentationlayer.dto.CreateMenuItemRequestDTO;
import com.example.restaurantreservation.menu.presentationlayer.dto.MenuItemResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/v1/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    // GET /api/v1/menu-items — 200 OK
    @GetMapping
    public ResponseEntity<List<MenuItemResponseDTO>> getAll() {
        return ResponseEntity.ok(menuItemService.findAll());
    }

    // GET /api/v1/menu-items/{id} — 200 OK | 404 Not Found
    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.findById(id));
    }

    // POST /api/v1/menu-items — 201 Created | 400 Bad Request
    @PostMapping
    public ResponseEntity<MenuItemResponseDTO> create(@RequestBody @Valid CreateMenuItemRequestDTO dto) {
        MenuItemResponseDTO created = menuItemService.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getMenuItemId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    // PUT /api/v1/menu-items/{id} — 200 OK | 404 Not Found
    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreateMenuItemRequestDTO dto) {
        return ResponseEntity.ok(menuItemService.update(id, dto));
    }

    // DELETE /api/v1/menu-items/{id} — 204 No Content | 404 Not Found
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
