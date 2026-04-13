package com.example.restaurantreservation.apigateway.presentation;

import com.example.restaurantreservation.apigateway.businesslogic.ReservationGatewayService;
import com.example.restaurantreservation.apigateway.presentation.dto.floor.CreateDiningTableRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.floor.DiningTableResponseDTO;
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
@RequestMapping("/api/v1/dining-tables")
@RequiredArgsConstructor
public class DiningTableGatewayController {

    private final ReservationGatewayService reservationGatewayService;

    @GetMapping
    public ResponseEntity<List<DiningTableResponseDTO>> getAll() {
        List<DiningTableResponseDTO> tables = reservationGatewayService.getAllDiningTables();
        tables.forEach(this::addLinks);
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiningTableResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addLinks(reservationGatewayService.getDiningTableById(id)));
    }

    @PostMapping
    public ResponseEntity<DiningTableResponseDTO> create(@RequestBody @Valid CreateDiningTableRequestDTO request) {
        DiningTableResponseDTO created = addLinks(reservationGatewayService.createDiningTable(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getTableId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiningTableResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreateDiningTableRequestDTO request) {
        return ResponseEntity.ok(addLinks(reservationGatewayService.updateDiningTable(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationGatewayService.deleteDiningTable(id);
        return ResponseEntity.noContent().build();
    }

    private DiningTableResponseDTO addLinks(DiningTableResponseDTO dto) {
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", link(linkTo(methodOn(DiningTableGatewayController.class).getById(dto.getTableId())).toUri().toString()));
        links.put("all-tables", link(linkTo(methodOn(DiningTableGatewayController.class).getAll()).toUri().toString()));
        dto.set_links(links);
        return dto;
    }

    private Map<String, Object> link(String href) {
        return Map.of("href", href);
    }
}
