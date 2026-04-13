package com.example.restaurantreservation.booking.presentationlayer;

import com.example.restaurantreservation.booking.businesslogiclayer.TableBookingService;
import com.example.restaurantreservation.booking.businesslogiclayer.TableBookingServiceImpl;
import com.example.restaurantreservation.booking.presentationlayer.dto.BookingResponseDTO;
import com.example.restaurantreservation.booking.presentationlayer.dto.CreateBookingRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class TableBookingController {

    private final TableBookingService bookingService;

    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getAll() {
        return ResponseEntity.ok(bookingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.findById(id));
    }

    @PostMapping
    public ResponseEntity<BookingResponseDTO> create(
            @RequestBody @Valid CreateBookingRequestDTO dto) {
        BookingResponseDTO created = bookingService.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getBookingId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreateBookingRequestDTO dto) {
        return ResponseEntity.ok(bookingService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
