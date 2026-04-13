package com.example.restaurantreservation.booking.domain;

import com.example.restaurantreservation.booking.domain.enums.PreOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "pre_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pre_order_id")
    private Long preOrderId;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency = "CAD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PreOrderStatus status = PreOrderStatus.DRAFT;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @ToString.Exclude
    @OneToMany(mappedBy = "preOrder", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<OrderLineItem> items = new ArrayList<>();
}

