package com.money.finance_tracker.service;

import com.money.finance_tracker.dto.CategorySpendingDto;
import com.money.finance_tracker.dto.DashboardResponseDto;
import com.money.finance_tracker.dto.FundingSourceResponseDto;
import com.money.finance_tracker.dto.MonthlySpendingDto;
import com.money.finance_tracker.entity.FundingSource;
import com.money.finance_tracker.entity.TransactionTypeEnum;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.repository.FundingSourceRepository;
import com.money.finance_tracker.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final FundingSourceRepository fundingSourceRepository;

    public DashboardResponseDto getOverview(User user, int year) {
        Long userId = user.getId();

        BigDecimal income = transactionRepository.sumAmountByUserAndType(
                userId,
                TransactionTypeEnum.INCOME
        );

        BigDecimal expense = transactionRepository.sumAmountByUserAndType(
                userId,
                TransactionTypeEnum.EXPENSE
        );

        BigDecimal currentTrackedMoney =
                fundingSourceRepository.sumBalancesByUserId(userId);

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = startDate.plusYears(1);

        List<MonthlySpendingDto> monthlySpending =
                transactionRepository.getMonthlySpending(
                        userId,
                        TransactionTypeEnum.EXPENSE,
                        startDate,
                        endDate
                );

        List<CategorySpendingDto> spendingByCategory =
                transactionRepository.getSpendingByCategory(
                        userId,
                        TransactionTypeEnum.EXPENSE,
                        startDate,
                        endDate
                );

        List<FundingSourceResponseDto> fundingSources =
                fundingSourceRepository.findAllByUserIdOrderByNameAsc(userId)
                        .stream()
                        .map(this::toFundingSourceDto)
                        .toList();

        return new DashboardResponseDto(
                income,
                expense,
                income.subtract(expense),
                currentTrackedMoney,
                fundingSources,
                monthlySpending,
                spendingByCategory
        );
    }

    private FundingSourceResponseDto toFundingSourceDto(
            FundingSource fundingSource
    ) {
        return new FundingSourceResponseDto(
                fundingSource.getId(),
                fundingSource.getName(),
                fundingSource.getBalance()
        );
    }
}
