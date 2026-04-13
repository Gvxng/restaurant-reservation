package com.example.restaurantreservation.loyalty.domain;

import com.example.restaurantreservation.loyalty.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "points_transactions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private LoyaltyAccount loyaltyAccount;

    @Column(nullable = false)
    private int points;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    // Cross-context ID reference to booking
    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
}
