package com.money.finance_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardResponseDto {

    private BigDecimal allTimeIncome;
    private BigDecimal allTimeExpense;
    private BigDecimal cashFlow;
    private BigDecimal currentTrackedMoney;

    private List<FundingSourceResponseDto> fundingSources;
    private List<MonthlySpendingDto> monthlySpending;
    private List<CategorySpendingDto> spendingByCategory;
}
