package com.example.restaurantreservation.floor.datamappinglayer;

import com.example.restaurantreservation.floor.domain.DiningTable;
import com.example.restaurantreservation.floor.presentationlayer.dto.DiningTableResponseDTO;
import com.example.restaurantreservation.floor.presentationlayer.dto.DiningTableSummaryDTO;
import org.springframework.stereotype.Component;

@Component
public class DiningTableMapper {

    public DiningTableResponseDTO toResponseDTO(DiningTable table) {
        return DiningTableResponseDTO.builder()
                .tableId(table.getTableId())
                .tableNumber(table.getTableNumber())
                .seatingCapacity(table.getSeatingCapacity())
                .tableType(table.getTableType())
                .status(table.getStatus())
                .sectionId(table.getSection() != null ? table.getSection().getSectionId() : null)
                .sectionName(table.getSection() != null ? table.getSection().getSectionName() : null)
                .positionX(table.getPositionX())
                .positionY(table.getPositionY())
                .build();
    }

    public DiningTableSummaryDTO toSummaryDTO(DiningTable table) {
        return DiningTableSummaryDTO.builder()
                .tableId(table.getTableId())
                .tableNumber(table.getTableNumber())
                .seatingCapacity(table.getSeatingCapacity())
                .tableType(table.getTableType())
                .status(table.getStatus())
                .sectionName(table.getSection() != null ? table.getSection().getSectionName() : null)
                .build();
    }
}
