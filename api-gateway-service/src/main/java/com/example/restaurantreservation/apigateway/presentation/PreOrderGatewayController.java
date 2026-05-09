package com.example.restaurantreservation.apigateway.presentation;

import com.example.restaurantreservation.apigateway.businesslogic.ReservationGatewayService;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.CreatePreOrderRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.PreOrderSummaryDTO;
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
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/pre-orders")
@RequiredArgsConstructor
@Tag(name = "Pre-Orders", description = "Pre-order endpoints linked to table bookings")
public class PreOrderGatewayController {

    private final ReservationGatewayService reservationGatewayService;

    @GetMapping("/{id}")
    @Operation(summary = "Get a pre-order by ID", description = "Returns one pre-order and its line items by pre-order ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pre-order returned successfully"),
            @ApiResponse(responseCode = "404", description = "Pre-order not found")
    })
    public ResponseEntity<PreOrderSummaryDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addLinks(reservationGatewayService.getPreOrderById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a pre-order", description = "Creates a pre-order for an existing table booking.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pre-order created successfully"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "409", description = "Booking cannot accept a pre-order")
    })
    public ResponseEntity<PreOrderSummaryDTO> create(@RequestBody @Valid CreatePreOrderRequestDTO request) {
        PreOrderSummaryDTO created = addLinks(reservationGatewayService.createPreOrder(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getPreOrderId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a pre-order", description = "Replaces the line items for an existing pre-order.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pre-order updated successfully"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation"),
            @ApiResponse(responseCode = "404", description = "Pre-order or booking not found"),
            @ApiResponse(responseCode = "409", description = "Booking cannot accept a pre-order")
    })
    public ResponseEntity<PreOrderSummaryDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreatePreOrderRequestDTO request) {
        return ResponseEntity.ok(addLinks(reservationGatewayService.updatePreOrder(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a pre-order", description = "Deletes a pre-order by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pre-order deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Pre-order not found")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationGatewayService.deletePreOrder(id);
        return ResponseEntity.noContent().build();
    }

    private PreOrderSummaryDTO addLinks(PreOrderSummaryDTO dto) {
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", link(linkTo(methodOn(PreOrderGatewayController.class).getById(dto.getPreOrderId())).toUri().toString()));
        links.put("booking", link(linkTo(methodOn(BookingGatewayController.class).getById(dto.getBookingId())).toUri().toString()));
        dto.set_links(links);
        return dto;
    }

    private Map<String, Object> link(String href) {
        return Map.of("href", href);
    }
}
