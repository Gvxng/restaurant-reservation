package com.example.restaurantreservation.loyalty.domain;

import com.example.restaurantreservation.loyalty.domain.enums.LoyaltyTier;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "loyalty_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;

    @Column(name = "points_balance", nullable = false)
    private int pointsBalance = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoyaltyTier tier = LoyaltyTier.BRONZE;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @ToString.Exclude
    @OneToMany(mappedBy = "loyaltyAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PointsTransaction> transactions = new ArrayList<>();
}
