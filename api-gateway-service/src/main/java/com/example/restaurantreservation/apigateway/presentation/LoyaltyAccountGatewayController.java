package com.example.restaurantreservation.apigateway.presentation;

import com.example.restaurantreservation.apigateway.businesslogic.LoyaltyGatewayService;
import com.example.restaurantreservation.apigateway.presentation.dto.loyalty.CreateLoyaltyAccountRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.loyalty.LoyaltyAccountResponseDTO;
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
@RequestMapping("/api/v1/loyalty-accounts")
@RequiredArgsConstructor
public class LoyaltyAccountGatewayController {

    private final LoyaltyGatewayService loyaltyGatewayService;

    @GetMapping
    public ResponseEntity<List<LoyaltyAccountResponseDTO>> getAll() {
        List<LoyaltyAccountResponseDTO> accounts = loyaltyGatewayService.getAllLoyaltyAccounts();
        accounts.forEach(this::addLinks);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoyaltyAccountResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addLinks(loyaltyGatewayService.getLoyaltyAccountById(id)));
    }

    @PostMapping
    public ResponseEntity<LoyaltyAccountResponseDTO> create(@RequestBody @Valid CreateLoyaltyAccountRequestDTO request) {
        LoyaltyAccountResponseDTO created = addLinks(loyaltyGatewayService.createLoyaltyAccount(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getAccountId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoyaltyAccountResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreateLoyaltyAccountRequestDTO request) {
        return ResponseEntity.ok(addLinks(loyaltyGatewayService.updateLoyaltyAccount(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        loyaltyGatewayService.deleteLoyaltyAccount(id);
        return ResponseEntity.noContent().build();
    }

    private LoyaltyAccountResponseDTO addLinks(LoyaltyAccountResponseDTO dto) {
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", link(linkTo(methodOn(LoyaltyAccountGatewayController.class).getById(dto.getAccountId())).toUri().toString()));
        links.put("all-accounts", link(linkTo(methodOn(LoyaltyAccountGatewayController.class).getAll()).toUri().toString()));
        dto.set_links(links);
        return dto;
    }

    private Map<String, Object> link(String href) {
        return Map.of("href", href);
    }
}
