package com.example.restaurantreservation.apigateway.presentation;

import com.example.restaurantreservation.apigateway.businesslogic.ReservationGatewayService;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.BookingResponseDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.booking.CreateBookingRequestDTO;
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
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingGatewayController {

    private final ReservationGatewayService reservationGatewayService;

    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getAll() {
        List<BookingResponseDTO> bookings = reservationGatewayService.getAllBookings();
        bookings.forEach(this::addLinks);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getById(@PathVariable Long id) {
        BookingResponseDTO booking = reservationGatewayService.getBookingById(id);
        return ResponseEntity.ok(addLinks(booking));
    }

    @PostMapping
    public ResponseEntity<BookingResponseDTO> create(@RequestBody @Valid CreateBookingRequestDTO request) {
        BookingResponseDTO created = addLinks(reservationGatewayService.createBooking(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getBookingId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreateBookingRequestDTO request) {
        return ResponseEntity.ok(addLinks(reservationGatewayService.updateBooking(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationGatewayService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    private BookingResponseDTO addLinks(BookingResponseDTO dto) {
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", link(linkTo(methodOn(BookingGatewayController.class).getById(dto.getBookingId())).toUri().toString()));
        links.put("all-bookings", link(linkTo(methodOn(BookingGatewayController.class).getAll()).toUri().toString()));
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
