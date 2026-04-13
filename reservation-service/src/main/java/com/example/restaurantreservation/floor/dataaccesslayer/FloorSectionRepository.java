package com.example.restaurantreservation.floor.dataaccesslayer;

import com.example.restaurantreservation.floor.domain.FloorSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FloorSectionRepository extends JpaRepository<FloorSection, Long> {}
