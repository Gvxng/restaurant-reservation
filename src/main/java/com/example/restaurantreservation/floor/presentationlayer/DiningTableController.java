package com.example.restaurantreservation.floor.presentationlayer;

import com.example.restaurantreservation.floor.businesslogiclayer.DiningTableService;
import com.example.restaurantreservation.floor.businesslogiclayer.DiningTableServiceImpl;
import com.example.restaurantreservation.floor.presentationlayer.dto.CreateDiningTableRequestDTO;
import com.example.restaurantreservation.floor.presentationlayer.dto.DiningTableResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/v1/dining-tables")
@RequiredArgsConstructor
public class DiningTableController {

    private final DiningTableService tableService;

    @GetMapping
    public ResponseEntity<List<DiningTableResponseDTO>> getAll() {
        return ResponseEntity.ok(tableService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiningTableResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tableService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DiningTableResponseDTO> create(@RequestBody @Valid CreateDiningTableRequestDTO dto) {
        DiningTableResponseDTO created = tableService.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getTableId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiningTableResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreateDiningTableRequestDTO dto) {
        return ResponseEntity.ok(tableService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tableService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
