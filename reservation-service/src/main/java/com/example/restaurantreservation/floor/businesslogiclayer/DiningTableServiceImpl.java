package com.example.restaurantreservation.floor.businesslogiclayer;

import com.example.restaurantreservation.exception.BusinessRuleViolationException;
import com.example.restaurantreservation.exception.DuplicateTableNumberException;
import com.example.restaurantreservation.exception.ResourceNotFoundException;
import com.example.restaurantreservation.floor.dataaccesslayer.DiningTableRepository;
import com.example.restaurantreservation.floor.dataaccesslayer.FloorSectionRepository;
import com.example.restaurantreservation.floor.datamappinglayer.DiningTableMapper;
import com.example.restaurantreservation.floor.domain.DiningTable;
import com.example.restaurantreservation.floor.domain.FloorSection;
import com.example.restaurantreservation.floor.domain.enums.TableStatus;
import com.example.restaurantreservation.floor.presentationlayer.dto.CreateDiningTableRequestDTO;
import com.example.restaurantreservation.floor.presentationlayer.dto.DiningTableResponseDTO;
import com.example.restaurantreservation.floor.presentationlayer.dto.DiningTableSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DiningTableServiceImpl implements DiningTableService {

    private final DiningTableRepository tableRepository;
    private final FloorSectionRepository sectionRepository;
    private final DiningTableMapper diningTableMapper;

    @Override
    public List<DiningTableResponseDTO> findAll() {
        List<DiningTable> tables = tableRepository.findAll();
        List<DiningTableResponseDTO> result = new ArrayList<>();
        for (DiningTable table : tables) {
            result.add(diningTableMapper.toResponseDTO(table));
        }
        return result;
    }

    @Override
    public DiningTableResponseDTO findById(Long id) {
        return diningTableMapper.toResponseDTO(getOrThrow(id));
    }

    @Override
    public DiningTableSummaryDTO getSummary(Long id) {
        return diningTableMapper.toSummaryDTO(getOrThrow(id));
    }

    @Override
    public DiningTableResponseDTO create(CreateDiningTableRequestDTO dto) {
        validateCapacity(dto.getSeatingCapacity());
        if (tableRepository.existsByTableNumber(dto.getTableNumber())) {
            throw new DuplicateTableNumberException("Table number '" + dto.getTableNumber() + "' already exists.");
        }

        DiningTable table = new DiningTable();
        applyDTO(table, dto);
        return diningTableMapper.toResponseDTO(tableRepository.save(table));
    }

    @Override
    public DiningTableResponseDTO update(Long id, CreateDiningTableRequestDTO dto) {
        DiningTable table = getOrThrow(id);
        validateCapacity(dto.getSeatingCapacity());

        if (!table.getTableNumber().equals(dto.getTableNumber())
                && tableRepository.existsByTableNumber(dto.getTableNumber())) {
            throw new DuplicateTableNumberException("Table number '" + dto.getTableNumber() + "' already exists.");
        }

        applyDTO(table, dto);
        return diningTableMapper.toResponseDTO(tableRepository.save(table));
    }

    @Override
    public void delete(Long id) {
        if (!tableRepository.existsById(id)) {
            throw new ResourceNotFoundException("DiningTable", id);
        }
        tableRepository.deleteById(id);
    }

    @Override
    public void assertReservable(Long tableId, int partySize) {
        DiningTable table = getOrThrow(tableId);
        if (table.getStatus() == TableStatus.MAINTENANCE) {
            throw new BusinessRuleViolationException(
                    "Table " + table.getTableNumber() + " is under maintenance and cannot be reserved.");
        }
        if (table.getStatus() == TableStatus.RESERVED || table.getStatus() == TableStatus.OCCUPIED) {
            throw new BusinessRuleViolationException(
                    "Table " + table.getTableNumber() + " is currently " + table.getStatus() + ".");
        }
        if (partySize > table.getSeatingCapacity()) {
            throw new BusinessRuleViolationException(
                    "Party size " + partySize + " exceeds table capacity of " + table.getSeatingCapacity() + ".");
        }
    }

    @Override
    public void setStatus(Long tableId, TableStatus status) {
        DiningTable table = getOrThrow(tableId);
        table.setStatus(status);
        tableRepository.save(table);
    }

    private DiningTable getOrThrow(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiningTable", id));
    }

    private void validateCapacity(int seatingCapacity) {
        if (seatingCapacity < 1) {
            throw new BusinessRuleViolationException("Seating capacity must be at least 1.");
        }
    }

    private void applyDTO(DiningTable table, CreateDiningTableRequestDTO dto) {
        table.setTableNumber(dto.getTableNumber());
        table.setSeatingCapacity(dto.getSeatingCapacity());
        table.setTableType(dto.getTableType());
        table.setStatus(dto.getStatus() != null ? dto.getStatus() : TableStatus.AVAILABLE);
        table.setPositionX(dto.getPositionX());
        table.setPositionY(dto.getPositionY());

        if (dto.getSectionId() != null) {
            FloorSection section = sectionRepository.findById(dto.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("FloorSection", dto.getSectionId()));
            table.setSection(section);
        }
    }
}
