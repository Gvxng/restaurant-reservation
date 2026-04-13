package com.example.restaurantreservation.floor.businesslogiclayer;

import com.example.restaurantreservation.floor.domain.enums.TableStatus;
import com.example.restaurantreservation.floor.presentationlayer.dto.CreateDiningTableRequestDTO;
import com.example.restaurantreservation.floor.presentationlayer.dto.DiningTableResponseDTO;
import com.example.restaurantreservation.floor.presentationlayer.dto.DiningTableSummaryDTO;

import java.util.List;


public interface DiningTableService {
    List<DiningTableResponseDTO> findAll();
    DiningTableResponseDTO findById(Long id);
    DiningTableSummaryDTO getSummary(Long id);
    DiningTableResponseDTO create(CreateDiningTableRequestDTO dto);
    DiningTableResponseDTO update(Long id, CreateDiningTableRequestDTO dto);
    void delete(Long id);
    void assertReservable(Long tableId, int partySize);
    void setStatus(Long tableId, TableStatus status);
}
