package com.example.restaurantreservation.apigateway.presentation.dto.reservation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiningTableSnapshotDTO {
    private Long tableId;
    private String tableNumber;
    private int seatingCapacity;
    private String tableType;
    private String status;
    private String sectionName;
}
