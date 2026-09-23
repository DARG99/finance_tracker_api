package com.money.finance_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthlySpendingDto {
    private int month; // 1 = January, 12 = December
    private BigDecimal amount;
}