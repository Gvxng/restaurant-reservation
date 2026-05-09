package com.example.restaurantreservation.aggregator.presentationlayer;

import com.example.restaurantreservation.aggregator.businesslogiclayer.ReservationAggregatorService;
import com.example.restaurantreservation.aggregator.presentationlayer.dto.CreateReservationRequestDTO;
import com.example.restaurantreservation.aggregator.presentationlayer.dto.ReservationAggregateResponseDTO;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationAggregatorController {

    private final ReservationAggregatorService reservationAggregatorService;

    @GetMapping
    public ResponseEntity<List<ReservationAggregateResponseDTO>> getAll() {
        return ResponseEntity.ok(reservationAggregatorService.findAll());
    }

    @GetMapping("/{aggregateId}")
    public ResponseEntity<ReservationAggregateResponseDTO> getById(@PathVariable String aggregateId) {
        return ResponseEntity.ok(reservationAggregatorService.findById(aggregateId));
    }

    @PostMapping
    public ResponseEntity<ReservationAggregateResponseDTO> create(
            @RequestBody @Valid CreateReservationRequestDTO request) {
        ReservationAggregateResponseDTO created = reservationAggregatorService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{aggregateId}")
                .buildAndExpand(created.getAggregateId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{aggregateId}")
    public ResponseEntity<ReservationAggregateResponseDTO> update(
            @PathVariable String aggregateId,
            @RequestBody @Valid CreateReservationRequestDTO request) {
        return ResponseEntity.ok(reservationAggregatorService.update(aggregateId, request));
    }

    @DeleteMapping("/{aggregateId}")
    public ResponseEntity<Void> delete(@PathVariable String aggregateId) {
        reservationAggregatorService.delete(aggregateId);
        return ResponseEntity.noContent().build();
    }
}
