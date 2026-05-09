package com.example.restaurantreservation.aggregator.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiningTableSnapshot {
    private Long tableId;
    private String tableNumber;
    private int seatingCapacity;
    private String tableType;
    private String status;
    private String sectionName;
}
