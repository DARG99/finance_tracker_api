package com.money.finance_tracker.service;

import com.money.finance_tracker.dto.DashboardResponseDto;
import com.money.finance_tracker.dto.CategorySpendingDto;
import com.money.finance_tracker.dto.MonthlySpendingDto;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.repository.FundingSourceRepository;
import com.money.finance_tracker.repository.TransactionRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.money.finance_tracker.entity.TransactionNature.NORMAL;
import static com.money.finance_tracker.entity.TransactionNature.REIMBURSEMENT;
import static com.money.finance_tracker.entity.TransactionTypeEnum.EXPENSE;
import static com.money.finance_tracker.entity.TransactionTypeEnum.INCOME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    @ParameterizedTest
    @CsvSource({"0, 100", "25, 75", "100, 0"})
    void categoryBreakdownSubtractsReimbursementsAndSortsByNetSpending(
            String refunded, String expectedNet
    ) {
        TransactionRepository transactions = mock(TransactionRepository.class);
        DashboardService service = new DashboardService(
                transactions, mock(FundingSourceRepository.class)
        );
        User user = new User();
        user.setId(1L);
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = start.plusYears(1);
        BigDecimal refund = new BigDecimal(refunded);
        when(transactions.getTotalByTypeAndNature(1L, INCOME, NORMAL))
                .thenReturn(new BigDecimal("500"));
        when(transactions.sumAmountByUserAndType(1L, EXPENSE))
                .thenReturn(new BigDecimal("180"));
        when(transactions.getTotalByTypeAndNature(1L, INCOME, REIMBURSEMENT))
                .thenReturn(refund);
        when(transactions.getSpendingByCategory(1L, EXPENSE, start, end))
                .thenReturn(List.of(
                        new CategorySpendingDto(10L, "Jantar", new BigDecimal("100")),
                        new CategorySpendingDto(20L, "Groceries", new BigDecimal("80"))
                ));
        when(transactions.getReimbursementsByExpenseCategory(
                1L, INCOME, REIMBURSEMENT, EXPENSE, start, end
        )).thenReturn(refund.signum() == 0 ? List.of()
                : List.of(new CategorySpendingDto(10L, "Jantar", refund)));

        DashboardResponseDto overview = service.getOverview(user, 2026);

        CategorySpendingDto dinner = new CategorySpendingDto(
                10L, "Jantar", new BigDecimal(expectedNet)
        );
        CategorySpendingDto groceries = new CategorySpendingDto(
                20L, "Groceries", new BigDecimal("80")
        );
        assertEquals(refund.signum() == 0 ? List.of(dinner, groceries)
                : List.of(groceries, dinner), overview.getSpendingByCategory());
        assertEquals(new BigDecimal("500"), overview.getAllTimeIncome());
        assertEquals(overview.getAllTimeExpense(), overview.getSpendingByCategory()
                .stream().map(CategorySpendingDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @ParameterizedTest
    @CsvSource({"0, 50", "25, 25", "50, 0"})
    void reimbursementsReduceOriginalMonthSpendingWithoutIncreasingIncome(
            String refunded, String expectedNet
    ) {
        TransactionRepository transactions = mock(TransactionRepository.class);
        FundingSourceRepository fundingSources = mock(FundingSourceRepository.class);
        DashboardService service = new DashboardService(transactions, fundingSources);
        User user = new User();
        user.setId(1L);
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = start.plusYears(1);
        BigDecimal refund = new BigDecimal(refunded);

        when(transactions.getTotalByTypeAndNature(1L, INCOME, NORMAL))
                .thenReturn(new BigDecimal("1000"));
        when(transactions.sumAmountByUserAndType(1L, EXPENSE))
                .thenReturn(new BigDecimal("70"));
        when(transactions.getTotalByTypeAndNature(1L, INCOME, REIMBURSEMENT))
                .thenReturn(refund);
        when(transactions.getMonthlySpending(1L, EXPENSE, start, end))
                .thenReturn(List.of(
                        new MonthlySpendingDto(1, new BigDecimal("20")),
                        new MonthlySpendingDto(12, new BigDecimal("50"))
                ));
        when(transactions.getMonthlyReimbursementsForExpenses(
                1L, INCOME, REIMBURSEMENT, EXPENSE, start, end
        )).thenReturn(refund.signum() == 0 ? List.of()
                : List.of(new MonthlySpendingDto(12, refund)));

        DashboardResponseDto overview = service.getOverview(user, 2025);

        assertEquals(new BigDecimal("1000"), overview.getAllTimeIncome());
        assertEquals(new BigDecimal(expectedNet).add(new BigDecimal("20")),
                overview.getAllTimeExpense());
        assertEquals(new BigDecimal("1000").subtract(overview.getAllTimeExpense()),
                overview.getCashFlow());
        assertEquals(List.of(
                new MonthlySpendingDto(1, new BigDecimal("20")),
                new MonthlySpendingDto(12, new BigDecimal(expectedNet))
        ), overview.getMonthlySpending());
    }
}
