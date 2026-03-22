package com.example.restaurantreservation.floor.datamappinglayer;

import com.example.restaurantreservation.floor.domain.DiningTable;
import com.example.restaurantreservation.floor.presentationlayer.dto.DiningTableResponseDTO;
import com.example.restaurantreservation.floor.presentationlayer.dto.DiningTableSummaryDTO;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;


@Component
public class DiningTableMapper {

    public DiningTableResponseDTO toResponseDTO(DiningTable t) {
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self",       Map.of("href", "/api/v1/dining-tables/" + t.getTableId()));
        links.put("all-tables", Map.of("href", "/api/v1/dining-tables"));
        if (t.getSection() != null) {
            links.put("section", Map.of("href", "/api/v1/floor-sections/" + t.getSection().getSectionId()));
        }

        return DiningTableResponseDTO.builder()
                .tableId(t.getTableId())
                .tableNumber(t.getTableNumber())
                .seatingCapacity(t.getSeatingCapacity())
                .tableType(t.getTableType())
                .status(t.getStatus())
                .sectionId(t.getSection() != null ? t.getSection().getSectionId() : null)
                .sectionName(t.getSection() != null ? t.getSection().getSectionName() : null)
                .positionX(t.getPositionX())
                .positionY(t.getPositionY())
                ._links(links)
                .build();
    }

    public DiningTableSummaryDTO toSummaryDTO(DiningTable t) {
        return DiningTableSummaryDTO.builder()
                .tableId(t.getTableId())
                .tableNumber(t.getTableNumber())
                .seatingCapacity(t.getSeatingCapacity())
                .tableType(t.getTableType())
                .status(t.getStatus())
                .sectionName(t.getSection() != null ? t.getSection().getSectionName() : null)
                .build();
    }
}
