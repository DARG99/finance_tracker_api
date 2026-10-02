package com.money.finance_tracker.service;

import com.money.finance_tracker.dto.CategorySpendingDto;
import com.money.finance_tracker.dto.DashboardResponseDto;
import com.money.finance_tracker.dto.FundingSourceResponseDto;
import com.money.finance_tracker.dto.MonthlySpendingDto;
import com.money.finance_tracker.entity.FundingSource;
import com.money.finance_tracker.entity.TransactionNature;
import com.money.finance_tracker.entity.TransactionTypeEnum;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.repository.FundingSourceRepository;
import com.money.finance_tracker.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final FundingSourceRepository fundingSourceRepository;

    public DashboardResponseDto getOverview(User user, int year) {
        Long userId = user.getId();

        // Only salary, sales, gifts, etc. Reimbursements are excluded.
        BigDecimal income = transactionRepository.getTotalByTypeAndNature(
                userId,
                TransactionTypeEnum.INCOME,
                TransactionNature.NORMAL
        );

        // Every outgoing expense before reimbursements.
        BigDecimal grossExpense = transactionRepository.sumAmountByUserAndType(
                userId,
                TransactionTypeEnum.EXPENSE
        );

        // Money that entered your account, but is not real income.
        BigDecimal reimbursements =
                transactionRepository.getTotalByTypeAndNature(
                        userId,
                        TransactionTypeEnum.INCOME,
                        TransactionNature.REIMBURSEMENT
                );

        // This is what you actually paid from your own money.
        BigDecimal netExpense = grossExpense.subtract(reimbursements);

        BigDecimal currentTrackedMoney =
                fundingSourceRepository.sumBalancesByUserId(userId);

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = startDate.plusYears(1);

        Map<Integer, BigDecimal> monthlyReimbursements = transactionRepository
                .getMonthlyReimbursementsForExpenses(
                        userId,
                        TransactionTypeEnum.INCOME,
                        TransactionNature.REIMBURSEMENT,
                        TransactionTypeEnum.EXPENSE,
                        startDate,
                        endDate
                ).stream().collect(Collectors.toMap(
                        MonthlySpendingDto::getMonth,
                        MonthlySpendingDto::getAmount
                ));

        List<MonthlySpendingDto> monthlySpending =
                transactionRepository.getMonthlySpending(
                        userId,
                        TransactionTypeEnum.EXPENSE,
                        startDate,
                        endDate
                ).stream().map(month -> new MonthlySpendingDto(
                        month.getMonth(),
                        month.getAmount().subtract(monthlyReimbursements.getOrDefault(
                                month.getMonth(), BigDecimal.ZERO
                        ))
                )).toList();

        Map<Long, BigDecimal> categoryReimbursements = transactionRepository
                .getReimbursementsByExpenseCategory(
                        userId,
                        TransactionTypeEnum.INCOME,
                        TransactionNature.REIMBURSEMENT,
                        TransactionTypeEnum.EXPENSE,
                        startDate,
                        endDate
                ).stream().collect(Collectors.toMap(
                        CategorySpendingDto::getCategoryId,
                        CategorySpendingDto::getAmount
                ));

        List<CategorySpendingDto> spendingByCategory =
                transactionRepository.getSpendingByCategory(
                        userId,
                        TransactionTypeEnum.EXPENSE,
                        startDate,
                        endDate
                ).stream().map(category -> new CategorySpendingDto(
                        category.getCategoryId(),
                        category.getCategoryName(),
                        category.getAmount().subtract(categoryReimbursements.getOrDefault(
                                category.getCategoryId(), BigDecimal.ZERO
                        ))
                )).sorted(Comparator.comparing(CategorySpendingDto::getAmount).reversed())
                        .toList();

        List<FundingSourceResponseDto> fundingSources =
                fundingSourceRepository.findAllByUserIdOrderByNameAsc(userId)
                        .stream()
                        .map(this::toFundingSourceDto)
                        .toList();

        return new DashboardResponseDto(
                income,                       // allTimeIncome
                netExpense,                   // allTimeExpense
                income.subtract(netExpense),  // cashFlow
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
