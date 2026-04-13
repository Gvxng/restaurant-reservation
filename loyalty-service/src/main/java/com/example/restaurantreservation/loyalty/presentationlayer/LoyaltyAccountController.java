package com.example.restaurantreservation.loyalty.presentationlayer;

import com.example.restaurantreservation.loyalty.businesslogiclayer.LoyaltyAccountService;
import com.example.restaurantreservation.loyalty.businesslogiclayer.LoyaltyAccountServiceImpl;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.CreateLoyaltyAccountRequestDTO;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.LoyaltyAccountResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/v1/loyalty-accounts")
@RequiredArgsConstructor
public class LoyaltyAccountController {

    private final LoyaltyAccountService loyaltyAccountService;

    @GetMapping
    public ResponseEntity<List<LoyaltyAccountResponseDTO>> getAll() {
        return ResponseEntity.ok(loyaltyAccountService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoyaltyAccountResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(loyaltyAccountService.findById(id));
    }

    @PostMapping
    public ResponseEntity<LoyaltyAccountResponseDTO> create(
            @RequestBody @Valid CreateLoyaltyAccountRequestDTO dto) {
        LoyaltyAccountResponseDTO created = loyaltyAccountService.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getAccountId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoyaltyAccountResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreateLoyaltyAccountRequestDTO dto) {
        return ResponseEntity.ok(loyaltyAccountService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        loyaltyAccountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
