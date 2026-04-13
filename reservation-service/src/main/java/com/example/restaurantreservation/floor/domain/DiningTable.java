package com.example.restaurantreservation.floor.domain;

import com.example.restaurantreservation.floor.domain.enums.TableStatus;
import com.example.restaurantreservation.floor.domain.enums.TableType;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "dining_tables")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiningTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "table_number", nullable = false, unique = true, length = 20)
    private String tableNumber;

    @Column(name = "seating_capacity", nullable = false)
    private int seatingCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_type", nullable = false, length = 30)
    private TableType tableType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TableStatus status = TableStatus.AVAILABLE;

    // Value Object: TableLocation (embedded as section + coordinates)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private FloorSection section;

    @Column(name = "position_x")
    private int positionX;

    @Column(name = "position_y")
    private int positionY;
}
