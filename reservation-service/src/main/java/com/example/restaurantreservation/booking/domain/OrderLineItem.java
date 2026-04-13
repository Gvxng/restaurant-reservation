package com.example.restaurantreservation.booking.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_line_items")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_item_id")
    private Long lineItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_order_id", nullable = false)
    private PreOrder preOrder;

    // Cross-context ID reference to MenuItem aggregate
    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;

    @Column(nullable = false)
    private int quantity;

    // Value Object: Money (embedded)
    @Column(name = "unit_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitAmount;

    @Column(nullable = false, length = 3)
    private String currency = "CAD";
}
