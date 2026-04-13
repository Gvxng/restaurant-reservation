package com.example.restaurantreservation.menu.domain;

import com.example.restaurantreservation.menu.domain.enums.MenuCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


@Entity
@Table(name = "menu_items")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_item_id")
    private Long menuItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    // Value Object: Money (embedded)
    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "CAD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MenuCategory category;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    // Value Object: DietaryTag (stored as comma-separated string for simplicity)
    @Column(name = "dietary_tags", length = 255)
    private String dietaryTags;
}
