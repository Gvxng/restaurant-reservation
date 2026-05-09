package com.example.restaurantreservation.apigateway.presentation;

import com.example.restaurantreservation.apigateway.businesslogic.LoyaltyGatewayService;
import com.example.restaurantreservation.apigateway.presentation.dto.loyalty.CreateLoyaltyAccountRequestDTO;
import com.example.restaurantreservation.apigateway.presentation.dto.loyalty.LoyaltyAccountResponseDTO;
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
@RequestMapping("/api/v1/loyalty-accounts")
@RequiredArgsConstructor
@Tag(name = "Loyalty Accounts", description = "Customer loyalty account endpoints")
public class LoyaltyAccountGatewayController {

    private final LoyaltyGatewayService loyaltyGatewayService;

    @GetMapping
    @Operation(summary = "Get all loyalty accounts", description = "Returns all loyalty accounts registered in the loyalty service.")
    @ApiResponse(responseCode = "200", description = "Loyalty accounts returned successfully")
    public ResponseEntity<List<LoyaltyAccountResponseDTO>> getAll() {
        List<LoyaltyAccountResponseDTO> accounts = loyaltyGatewayService.getAllLoyaltyAccounts();
        accounts.forEach(this::addLinks);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a loyalty account by ID", description = "Returns one loyalty account by its database ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loyalty account returned successfully"),
            @ApiResponse(responseCode = "404", description = "Loyalty account not found")
    })
    public ResponseEntity<LoyaltyAccountResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addLinks(loyaltyGatewayService.getLoyaltyAccountById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a loyalty account", description = "Creates a loyalty account for a customer.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Loyalty account created successfully"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation"),
            @ApiResponse(responseCode = "409", description = "Customer already has a loyalty account")
    })
    public ResponseEntity<LoyaltyAccountResponseDTO> create(@RequestBody @Valid CreateLoyaltyAccountRequestDTO request) {
        LoyaltyAccountResponseDTO created = addLinks(loyaltyGatewayService.createLoyaltyAccount(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getAccountId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a loyalty account", description = "Updates loyalty points, tier, or enrollment date for an existing account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loyalty account updated successfully"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation"),
            @ApiResponse(responseCode = "404", description = "Loyalty account not found"),
            @ApiResponse(responseCode = "409", description = "Loyalty account business rule violation")
    })
    public ResponseEntity<LoyaltyAccountResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreateLoyaltyAccountRequestDTO request) {
        return ResponseEntity.ok(addLinks(loyaltyGatewayService.updateLoyaltyAccount(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a loyalty account", description = "Deletes a loyalty account by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Loyalty account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Loyalty account not found")
    })
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
