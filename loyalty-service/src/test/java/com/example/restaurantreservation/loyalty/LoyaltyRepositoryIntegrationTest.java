package com.example.restaurantreservation.loyalty;

import com.example.restaurantreservation.loyalty.dataaccesslayer.LoyaltyAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("testing")
class LoyaltyRepositoryIntegrationTest {

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Test
    void findByCustomerIdReturnsSeededAccount() {
        assertThat(loyaltyAccountRepository.findByCustomerId(101L))
                .isPresent()
                .get()
                .extracting("pointsBalance")
                .isEqualTo(1500);
    }

    @Test
    void findByCustomerIdReturnsEmptyForUnknownCustomer() {
        assertThat(loyaltyAccountRepository.findByCustomerId(999L)).isEmpty();
    }

    @Test
    void existsByCustomerIdReturnsTrueForSeededCustomer() {
        assertThat(loyaltyAccountRepository.existsByCustomerId(102L)).isTrue();
    }

    @Test
    void existsByCustomerIdReturnsFalseForUnknownCustomer() {
        assertThat(loyaltyAccountRepository.existsByCustomerId(555L)).isFalse();
    }
}
