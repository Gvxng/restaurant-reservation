package com.example.restaurantreservation.loyalty.businesslogiclayer;

import com.example.restaurantreservation.exception.BusinessRuleViolationException;
import com.example.restaurantreservation.exception.ResourceNotFoundException;
import com.example.restaurantreservation.loyalty.domain.LoyaltyAccount;
import com.example.restaurantreservation.loyalty.domain.PointsTransaction;
import com.example.restaurantreservation.loyalty.domain.enums.LoyaltyTier;
import com.example.restaurantreservation.loyalty.domain.enums.TransactionType;
import com.example.restaurantreservation.loyalty.dataaccesslayer.LoyaltyAccountRepository;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.CreateLoyaltyAccountRequestDTO;
import com.example.restaurantreservation.loyalty.datamappinglayer.LoyaltyAccountMapper;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.LoyaltyAccountResponseDTO;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.LoyaltyAccountSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class LoyaltyAccountServiceImpl implements LoyaltyAccountService {

    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyAccountMapper loyaltyAccountMapper; // Data Mapping Layer

    @Override
    public List<LoyaltyAccountResponseDTO> findAll() {
        List<LoyaltyAccount> accounts = accountRepository.findAll();
        List<LoyaltyAccountResponseDTO> result = new ArrayList<>();
        for (LoyaltyAccount a : accounts) {
            result.add(loyaltyAccountMapper.toResponseDTO(a));
        }
        return result;
    }
    @Override
    public LoyaltyAccountResponseDTO findById(Long id) {
        return loyaltyAccountMapper.toResponseDTO(getOrThrow(id));
    }
    @Override
    public LoyaltyAccountSummaryDTO getSummaryByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId)
                .map(loyaltyAccountMapper::toSummaryDTO)
                .orElse(null);
    }
    @Override
    public LoyaltyAccountResponseDTO create(CreateLoyaltyAccountRequestDTO dto) {
        // INV-9: one account per customer
        if (accountRepository.existsByCustomerId(dto.getCustomerId())) {
            throw new BusinessRuleViolationException(
                    "Customer " + dto.getCustomerId() + " already has a loyalty account.");
        }
        LoyaltyAccount acct = LoyaltyAccount.builder()
                .customerId(dto.getCustomerId())
                .pointsBalance(Math.max(0, dto.getPointsBalance()))
                .tier(dto.getTier() != null ? dto.getTier() : LoyaltyTier.BRONZE)
                .enrollmentDate(dto.getEnrollmentDate() != null ? dto.getEnrollmentDate() : LocalDate.now())
                .build();
        return loyaltyAccountMapper.toResponseDTO(accountRepository.save(acct));
    }
    @Override
    public LoyaltyAccountResponseDTO update(Long id, CreateLoyaltyAccountRequestDTO dto) {
        LoyaltyAccount acct = getOrThrow(id);
        // INV-8: balance cannot be negative
        if (dto.getPointsBalance() < 0) {
            throw new BusinessRuleViolationException("Points balance cannot be negative.");
        }
        acct.setPointsBalance(dto.getPointsBalance());
        if (dto.getTier() != null) acct.setTier(dto.getTier());
        if (dto.getEnrollmentDate() != null) acct.setEnrollmentDate(dto.getEnrollmentDate());
        return loyaltyAccountMapper.toResponseDTO(accountRepository.save(acct));
    }
    @Override
    public void delete(Long id) {
        if (!accountRepository.existsById(id)) throw new ResourceNotFoundException("LoyaltyAccount", id);
        accountRepository.deleteById(id);
    }

    /**
     * Called by TableBookingService when a booking is COMPLETED.
     * Credits points and upgrades tier automatically.
     */
    @Override
    public int earnPoints(Long customerId, Long bookingId, int points) {
        LoyaltyAccount acct = accountRepository.findByCustomerId(customerId).orElse(null);
        if (acct == null) return 0;

        acct.setPointsBalance(acct.getPointsBalance() + points);
        upgradeTierIfEligible(acct);

        PointsTransaction tx = PointsTransaction.builder()
                .loyaltyAccount(acct)
                .points(points)
                .transactionType(TransactionType.EARNED)
                .referenceId(bookingId)
                .transactionDate(LocalDateTime.now())
                .build();
        acct.getTransactions().add(tx);
        accountRepository.save(acct);
        return acct.getPointsBalance();
    }

    // ---- Private helpers ----
    private LoyaltyAccount getOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyAccount", id));
    }

    private void upgradeTierIfEligible(LoyaltyAccount acct) {
        int balance = acct.getPointsBalance();
        if (balance >= 5000)      acct.setTier(LoyaltyTier.PLATINUM);
        else if (balance >= 2000) acct.setTier(LoyaltyTier.GOLD);
        else if (balance >= 1000) acct.setTier(LoyaltyTier.SILVER);
        else                      acct.setTier(LoyaltyTier.BRONZE);
    }
}
