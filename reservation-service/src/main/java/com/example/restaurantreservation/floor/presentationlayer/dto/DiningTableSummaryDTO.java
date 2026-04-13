package com.example.restaurantreservation.floor.presentationlayer.dto;

import com.example.restaurantreservation.floor.domain.enums.TableStatus;
import com.example.restaurantreservation.floor.domain.enums.TableType;
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
