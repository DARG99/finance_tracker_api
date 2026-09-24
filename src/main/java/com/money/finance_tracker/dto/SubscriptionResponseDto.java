package com.money.finance_tracker.dto;

import com.money.finance_tracker.entity.SubscriptionFrequency;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SubscriptionResponseDto {

    private Long id;

    private String name;
    private BigDecimal amount;
    private SubscriptionFrequency frequency;
    private LocalDate nextPaymentDate;
    private boolean active;

    private Long fundingSourceId;
    private String fundingSourceName;

    private Long categoryId;
    private String categoryName;
}