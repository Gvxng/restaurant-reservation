package com.example.restaurantreservation.apigateway.presentation;

import com.example.restaurantreservation.apigateway.businesslogic.ReservationGatewayService;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.CreatePreOrderRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.PreOrderSummaryDTO;
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
public class PreOrderGatewayController {

    private final ReservationGatewayService reservationGatewayService;

    @GetMapping("/{id}")
    public ResponseEntity<PreOrderSummaryDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addLinks(reservationGatewayService.getPreOrderById(id)));
    }

    @PostMapping
    public ResponseEntity<PreOrderSummaryDTO> create(@RequestBody @Valid CreatePreOrderRequestDTO request) {
        PreOrderSummaryDTO created = addLinks(reservationGatewayService.createPreOrder(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getPreOrderId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PreOrderSummaryDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreatePreOrderRequestDTO request) {
        return ResponseEntity.ok(addLinks(reservationGatewayService.updatePreOrder(id, request)));
    }

    @DeleteMapping("/{id}")
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
