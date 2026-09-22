package com.money.finance_tracker.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionUpdateDto {

    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    private Long sourceFundingSourceId;
    private Long destinationFundingSourceId;
    private Long categoryId;

    private String description;
    private LocalDate transactionDate;
}