package com.example.restaurantreservation.apigateway.presentation;

import com.example.restaurantreservation.apigateway.businesslogic.ReservationGatewayService;
import com.example.restaurantreservation.apigateway.presentation.dto.floor.CreateDiningTableRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.floor.DiningTableResponseDTO;
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
@RequestMapping("/api/v1/dining-tables")
@RequiredArgsConstructor
@Tag(name = "Dining Tables", description = "Dining room table and floor layout endpoints")
public class DiningTableGatewayController {

    private final ReservationGatewayService reservationGatewayService;

    @GetMapping
    @Operation(summary = "Get all dining tables", description = "Returns all configured restaurant dining tables.")
    @ApiResponse(responseCode = "200", description = "Dining tables returned successfully")
    public ResponseEntity<List<DiningTableResponseDTO>> getAll() {
        List<DiningTableResponseDTO> tables = reservationGatewayService.getAllDiningTables();
        tables.forEach(this::addLinks);
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a dining table by ID", description = "Returns one dining table by its database ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dining table returned successfully"),
            @ApiResponse(responseCode = "404", description = "Dining table not found")
    })
    public ResponseEntity<DiningTableResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addLinks(reservationGatewayService.getDiningTableById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a dining table", description = "Creates a dining table in a restaurant floor section.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dining table created successfully"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation"),
            @ApiResponse(responseCode = "404", description = "Floor section not found"),
            @ApiResponse(responseCode = "409", description = "Dining table business rule violation")
    })
    public ResponseEntity<DiningTableResponseDTO> create(@RequestBody @Valid CreateDiningTableRequestDTO request) {
        DiningTableResponseDTO created = addLinks(reservationGatewayService.createDiningTable(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getTableId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a dining table", description = "Updates a dining table by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dining table updated successfully"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation"),
            @ApiResponse(responseCode = "404", description = "Dining table or floor section not found"),
            @ApiResponse(responseCode = "409", description = "Dining table business rule violation")
    })
    public ResponseEntity<DiningTableResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreateDiningTableRequestDTO request) {
        return ResponseEntity.ok(addLinks(reservationGatewayService.updateDiningTable(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a dining table", description = "Deletes a dining table by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dining table deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Dining table not found")
    })
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
