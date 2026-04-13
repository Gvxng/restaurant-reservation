package com.example.restaurantreservation.apigateway.presentation.dto.floor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateDiningTableRequestDTO {

    @NotBlank(message = "Table number is required")
    private String tableNumber;

    @Min(value = 1, message = "Seating capacity must be at least 1")
    private int seatingCapacity;

    @NotNull(message = "Table type is required")
    private TableType tableType;

    private TableStatus status = TableStatus.AVAILABLE;

    private Long sectionId;
    private int positionX;
    private int positionY;
}
