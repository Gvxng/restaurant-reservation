package com.example.restaurantreservation.loyalty;

import com.example.restaurantreservation.exception.ResourceNotFoundException;
import com.example.restaurantreservation.loyalty.businesslogiclayer.LoyaltyAccountService;
import com.example.restaurantreservation.loyalty.dataaccesslayer.LoyaltyAccountRepository;
import com.example.restaurantreservation.loyalty.domain.enums.LoyaltyTier;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.CreateLoyaltyAccountRequestDTO;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.LoyaltyAccountResponseDTO;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.LoyaltyAccountSummaryDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class LoyaltyServiceIntegrationTest {

    @Autowired
    private LoyaltyAccountService loyaltyAccountService;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Test
    void getSummaryByCustomerIdReturnsProjectionAndNullWhenMissing() {
        LoyaltyAccountSummaryDTO summary = loyaltyAccountService.getSummaryByCustomerId(101L);

        assertThat(summary).isNotNull();
        assertThat(summary.getCustomerId()).isEqualTo(101L);
        assertThat(summary.getTier()).isEqualTo(LoyaltyTier.SILVER);
        assertThat(loyaltyAccountService.getSummaryByCustomerId(999L)).isNull();
    }

    @Test
    void createWithNegativePointsUsesDefaultValues() {
        CreateLoyaltyAccountRequestDTO request = new CreateLoyaltyAccountRequestDTO();
        request.setCustomerId(9999L);
        request.setPointsBalance(-50);

        LoyaltyAccountResponseDTO created = loyaltyAccountService.create(request);

        assertThat(created.getCustomerId()).isEqualTo(9999L);
        assertThat(created.getPointsBalance()).isZero();
        assertThat(created.getTier()).isEqualTo(LoyaltyTier.BRONZE);
        assertThat(created.getEnrollmentDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void earnPointsCoversMissingAccountAndAllTierTransitions() {
        assertThat(loyaltyAccountService.earnPoints(99999L, 1L, 100)).isZero();

        assertThat(loyaltyAccountService.earnPoints(102L, 10L, 100)).isEqualTo(350);
        assertThat(loyaltyAccountRepository.findByCustomerId(102L).orElseThrow().getTier()).isEqualTo(LoyaltyTier.BRONZE);

        assertThat(loyaltyAccountService.earnPoints(102L, 11L, 700)).isEqualTo(1050);
        assertThat(loyaltyAccountRepository.findByCustomerId(102L).orElseThrow().getTier()).isEqualTo(LoyaltyTier.SILVER);

        assertThat(loyaltyAccountService.earnPoints(102L, 12L, 1000)).isEqualTo(2050);
        assertThat(loyaltyAccountRepository.findByCustomerId(102L).orElseThrow().getTier()).isEqualTo(LoyaltyTier.GOLD);

        assertThat(loyaltyAccountService.earnPoints(102L, 13L, 3000)).isEqualTo(5050);
        assertThat(loyaltyAccountRepository.findByCustomerId(102L).orElseThrow().getTier()).isEqualTo(LoyaltyTier.PLATINUM);
        assertThat(loyaltyAccountRepository.findByCustomerId(102L).orElseThrow().getTransactions()).hasSize(4);
    }

    @Test
    void deletingUnknownAccountThrowsNotFound() {
        assertThatThrownBy(() -> loyaltyAccountService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("LoyaltyAccount");
    }
}
