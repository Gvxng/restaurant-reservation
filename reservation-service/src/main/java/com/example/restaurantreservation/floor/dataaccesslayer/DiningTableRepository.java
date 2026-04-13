package com.example.restaurantreservation.floor.dataaccesslayer;

import com.example.restaurantreservation.floor.domain.DiningTable;
import com.example.restaurantreservation.floor.domain.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
    List<DiningTable> findByStatus(TableStatus status);
    List<DiningTable> findBySectionSectionId(Long sectionId);
    boolean existsByTableNumber(String tableNumber);
}
