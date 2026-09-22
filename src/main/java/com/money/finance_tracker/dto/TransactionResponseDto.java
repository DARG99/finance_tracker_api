package com.money.finance_tracker.dto;

import com.money.finance_tracker.entity.TransactionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {

    private Long id;

    private TransactionTypeEnum type;
    private BigDecimal amount;

    private Long sourceFundingSourceId;
    private String sourceFundingSourceName;

    private Long destinationFundingSourceId;
    private String destinationFundingSourceName;

    private Long categoryId;
    private String categoryName;

    private String description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;
}