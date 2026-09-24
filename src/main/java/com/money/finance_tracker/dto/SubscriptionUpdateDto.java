package com.money.finance_tracker.dto;

import com.money.finance_tracker.entity.SubscriptionFrequency;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SubscriptionUpdateDto {

    private String name;

    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    private Long fundingSourceId;
    private Long categoryId;

    private SubscriptionFrequency frequency;
    private LocalDate nextPaymentDate;

    private Boolean active;
}