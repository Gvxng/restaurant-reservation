package com.example.restaurantreservation.booking.presentationlayer;

import com.example.restaurantreservation.booking.businesslogiclayer.TableBookingService;
import com.example.restaurantreservation.booking.businesslogiclayer.TableBookingServiceImpl;
import com.example.restaurantreservation.booking.presentationlayer.dto.CreatePreOrderRequestDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.PreOrderSummaryDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;


@RestController
@RequestMapping("/api/v1/pre-orders")
@RequiredArgsConstructor
public class PreOrderController {

    private final TableBookingService bookingService;

    @GetMapping("/{id}")
    public ResponseEntity<PreOrderSummaryDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getPreOrder(id));
    }

    @PostMapping
    public ResponseEntity<PreOrderSummaryDTO> create(
            @RequestBody @Valid CreatePreOrderRequestDTO dto) {
        PreOrderSummaryDTO created = bookingService.createPreOrder(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getPreOrderId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PreOrderSummaryDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreatePreOrderRequestDTO dto) {
        return ResponseEntity.ok(bookingService.updatePreOrder(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookingService.deletePreOrder(id);
        return ResponseEntity.noContent().build();
    }
}
