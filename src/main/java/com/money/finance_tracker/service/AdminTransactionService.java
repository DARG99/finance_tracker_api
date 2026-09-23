package com.money.finance_tracker.service;

import com.money.finance_tracker.entity.FundingSource;
import com.money.finance_tracker.repository.FundingSourceRepository;
import com.money.finance_tracker.repository.TransactionRepository;
import com.money.finance_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final FundingSourceRepository fundingSourceRepository;

    @Transactional
    public int deleteAllTransactionsForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        int deletedTransactions =
                transactionRepository.deleteAllByUserId(userId);

        List<FundingSource> fundingSources =
                fundingSourceRepository
                        .findAllByUserIdOrderByNameAsc(userId);

        for (FundingSource fundingSource : fundingSources) {
            fundingSource.setBalance(BigDecimal.ZERO);
        }

        return deletedTransactions;
    }
}