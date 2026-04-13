package com.example.restaurantreservation.loyalty.dataaccesslayer;

import com.example.restaurantreservation.loyalty.domain.PointsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {
    List<PointsTransaction> findByLoyaltyAccountAccountId(Long accountId);
}
