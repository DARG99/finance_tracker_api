package com.money.finance_tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReimbursableExpenseResponseDto {

    private Long id;
    private String description;
    private LocalDate transactionDate;

    private BigDecimal amount;
    private BigDecimal alreadyReimbursedAmount;
    private BigDecimal remainingReimbursableAmount;

    private String categoryName;
    private String sourceFundingSourceName;
}