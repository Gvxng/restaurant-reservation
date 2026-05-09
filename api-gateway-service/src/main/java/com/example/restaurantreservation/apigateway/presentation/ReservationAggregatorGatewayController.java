package com.example.restaurantreservation.apigateway.presentation;

import com.example.restaurantreservation.apigateway.businesslogic.AggregatorGatewayService;
import com.example.restaurantreservation.apigateway.presentation.dto.reservation.CreateReservationRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.reservation.ReservationAggregateResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation Aggregates", description = "Aggregator endpoints that orchestrate bookings, pre-orders, menu items, and loyalty accounts")
public class ReservationAggregatorGatewayController {

    private final AggregatorGatewayService aggregatorGatewayService;

    @GetMapping
    @Operation(summary = "Get all reservation aggregates")
    @ApiResponse(responseCode = "200", description = "Reservation aggregates returned successfully")
    public ResponseEntity<List<ReservationAggregateResponseDTO>> getAll() {
        List<ReservationAggregateResponseDTO> reservations = aggregatorGatewayService.getAllReservations();
        reservations.forEach(this::addLinks);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{aggregateId}")
    @Operation(summary = "Get a reservation aggregate by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation aggregate returned successfully"),
            @ApiResponse(responseCode = "404", description = "Reservation aggregate not found")
    })
    public ResponseEntity<ReservationAggregateResponseDTO> getById(@PathVariable String aggregateId) {
        return ResponseEntity.ok(addLinks(aggregatorGatewayService.getReservationById(aggregateId)));
    }

    @PostMapping
    @Operation(summary = "Create a reservation aggregate")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reservation aggregate created successfully"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation"),
            @ApiResponse(responseCode = "409", description = "Reservation aggregate business rule violation")
    })
    public ResponseEntity<ReservationAggregateResponseDTO> create(
            @RequestBody @Valid CreateReservationRequestDTO request) {
        ReservationAggregateResponseDTO created = addLinks(aggregatorGatewayService.createReservation(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{aggregateId}")
                .buildAndExpand(created.getAggregateId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{aggregateId}")
    @Operation(summary = "Update a reservation aggregate")
    public ResponseEntity<ReservationAggregateResponseDTO> update(
            @PathVariable String aggregateId,
            @RequestBody @Valid CreateReservationRequestDTO request) {
        return ResponseEntity.ok(addLinks(aggregatorGatewayService.updateReservation(aggregateId, request)));
    }

    @DeleteMapping("/{aggregateId}")
    @Operation(summary = "Delete a reservation aggregate")
    public ResponseEntity<Void> delete(@PathVariable String aggregateId) {
        aggregatorGatewayService.deleteReservation(aggregateId);
        return ResponseEntity.noContent().build();
    }

    private ReservationAggregateResponseDTO addLinks(ReservationAggregateResponseDTO dto) {
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", link(linkTo(methodOn(ReservationAggregatorGatewayController.class).getById(dto.getAggregateId())).toUri().toString()));
        links.put("all-reservations", link(linkTo(methodOn(ReservationAggregatorGatewayController.class).getAll()).toUri().toString()));
        links.put("booking", link(linkTo(methodOn(BookingGatewayController.class).getById(dto.getBookingId())).toUri().toString()));
        links.put("table", link(linkTo(methodOn(DiningTableGatewayController.class).getById(dto.getTableId())).toUri().toString()));
        if (dto.getPreOrderId() != null) {
            links.put("pre-order", link(linkTo(methodOn(PreOrderGatewayController.class).getById(dto.getPreOrderId())).toUri().toString()));
        }
        dto.set_links(links);
        return dto;
    }

    private Map<String, Object> link(String href) {
        return Map.of("href", href);
    }
}
