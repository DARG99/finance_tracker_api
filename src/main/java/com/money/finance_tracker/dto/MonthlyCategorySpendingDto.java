package com.money.finance_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthlyCategorySpendingDto {
    private Integer month;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
}
