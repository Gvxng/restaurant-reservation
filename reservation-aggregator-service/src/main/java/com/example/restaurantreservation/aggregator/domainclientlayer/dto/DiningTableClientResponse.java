package com.example.restaurantreservation.aggregator.domainclientlayer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiningTableClientResponse {
    private Long tableId;
    private String tableNumber;
    private int seatingCapacity;
    private String tableType;
    private String status;
    private String sectionName;
}
