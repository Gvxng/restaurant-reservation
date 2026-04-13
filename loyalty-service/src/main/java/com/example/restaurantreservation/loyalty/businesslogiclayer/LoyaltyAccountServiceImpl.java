package com.example.restaurantreservation.loyalty.businesslogiclayer;

import com.example.restaurantreservation.exception.DuplicateLoyaltyAccountException;
import com.example.restaurantreservation.exception.NegativePointsBalanceException;
import com.example.restaurantreservation.exception.ResourceNotFoundException;
import com.example.restaurantreservation.loyalty.dataaccesslayer.LoyaltyAccountRepository;
import com.example.restaurantreservation.loyalty.datamappinglayer.LoyaltyAccountMapper;
import com.example.restaurantreservation.loyalty.domain.LoyaltyAccount;
import com.example.restaurantreservation.loyalty.domain.PointsTransaction;
import com.example.restaurantreservation.loyalty.domain.enums.LoyaltyTier;
import com.example.restaurantreservation.loyalty.domain.enums.TransactionType;
import com.example.restaurantreservation.loyalty.presentationlayer.dto.CreateLoyaltyAccountRequestDTO;
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
    private final LoyaltyAccountMapper loyaltyAccountMapper;

    @Override
    public List<LoyaltyAccountResponseDTO> findAll() {
        List<LoyaltyAccount> accounts = accountRepository.findAll();
        List<LoyaltyAccountResponseDTO> result = new ArrayList<>();
        for (LoyaltyAccount account : accounts) {
            result.add(loyaltyAccountMapper.toResponseDTO(account));
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
        if (accountRepository.existsByCustomerId(dto.getCustomerId())) {
            throw new DuplicateLoyaltyAccountException(
                    "Customer " + dto.getCustomerId() + " already has a loyalty account.");
        }

        LoyaltyAccount account = LoyaltyAccount.builder()
                .customerId(dto.getCustomerId())
                .pointsBalance(Math.max(0, dto.getPointsBalance()))
                .tier(dto.getTier() != null ? dto.getTier() : LoyaltyTier.BRONZE)
                .enrollmentDate(dto.getEnrollmentDate() != null ? dto.getEnrollmentDate() : LocalDate.now())
                .build();

        return loyaltyAccountMapper.toResponseDTO(accountRepository.save(account));
    }

    @Override
    public LoyaltyAccountResponseDTO update(Long id, CreateLoyaltyAccountRequestDTO dto) {
        LoyaltyAccount account = getOrThrow(id);
        if (dto.getPointsBalance() < 0) {
            throw new NegativePointsBalanceException("Points balance cannot be negative.");
        }

        account.setPointsBalance(dto.getPointsBalance());
        if (dto.getTier() != null) {
            account.setTier(dto.getTier());
        }
        if (dto.getEnrollmentDate() != null) {
            account.setEnrollmentDate(dto.getEnrollmentDate());
        }

        return loyaltyAccountMapper.toResponseDTO(accountRepository.save(account));
    }

    @Override
    public void delete(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new ResourceNotFoundException("LoyaltyAccount", id);
        }
        accountRepository.deleteById(id);
    }

    @Override
    public int earnPoints(Long customerId, Long bookingId, int points) {
        LoyaltyAccount account = accountRepository.findByCustomerId(customerId).orElse(null);
        if (account == null) {
            return 0;
        }

        account.setPointsBalance(account.getPointsBalance() + points);
        upgradeTierIfEligible(account);

        PointsTransaction transaction = PointsTransaction.builder()
                .loyaltyAccount(account)
                .points(points)
                .transactionType(TransactionType.EARNED)
                .referenceId(bookingId)
                .transactionDate(LocalDateTime.now())
                .build();

        account.getTransactions().add(transaction);
        accountRepository.save(account);
        return account.getPointsBalance();
    }

    private LoyaltyAccount getOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyAccount", id));
    }

    private void upgradeTierIfEligible(LoyaltyAccount account) {
        int balance = account.getPointsBalance();
        if (balance >= 5000) {
            account.setTier(LoyaltyTier.PLATINUM);
        } else if (balance >= 2000) {
            account.setTier(LoyaltyTier.GOLD);
        } else if (balance >= 1000) {
            account.setTier(LoyaltyTier.SILVER);
        } else {
            account.setTier(LoyaltyTier.BRONZE);
        }
    }
}
