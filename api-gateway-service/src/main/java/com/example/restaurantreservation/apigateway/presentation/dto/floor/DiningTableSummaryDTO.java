package com.example.restaurantreservation.apigateway.presentation.dto.floor;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiningTableSummaryDTO {
    private Long tableId;
    private String tableNumber;
    private int seatingCapacity;
    private TableType tableType;
    private TableStatus status;
    private String sectionName;
}
