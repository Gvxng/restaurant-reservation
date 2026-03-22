package com.example.restaurantreservation.floor.presentationlayer.dto;

import com.example.restaurantreservation.floor.domain.enums.TableStatus;
import com.example.restaurantreservation.floor.domain.enums.TableType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiningTableResponseDTO {
    private Long tableId;
    private String tableNumber;
    private int seatingCapacity;
    private TableType tableType;
    private TableStatus status;
    private Long sectionId;
    private String sectionName;
    private int positionX;
    private int positionY;

    @JsonProperty("_links")
    private Map<String, Object> _links;}
