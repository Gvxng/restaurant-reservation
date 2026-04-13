package com.example.restaurantreservation.loyalty.dataaccesslayer;

import com.example.restaurantreservation.loyalty.domain.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {
    Optional<LoyaltyAccount> findByCustomerId(Long customerId);
    boolean existsByCustomerId(Long customerId);
}
