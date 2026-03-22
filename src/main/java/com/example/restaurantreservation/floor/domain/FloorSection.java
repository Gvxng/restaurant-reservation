package com.example.restaurantreservation.floor.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "floor_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloorSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "section_name", nullable = false, length = 100)
    private String sectionName;

    @ToString.Exclude
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DiningTable> tables = new ArrayList<>();
}

