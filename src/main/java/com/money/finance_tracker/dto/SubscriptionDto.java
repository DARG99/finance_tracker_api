package com.money.finance_tracker.dto;

import com.money.finance_tracker.entity.SubscriptionFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SubscriptionDto {

    @NotBlank(message = "Subscription name is required")
    private String name;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Funding source is required")
    private Long fundingSourceId;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Frequency is required")
    private SubscriptionFrequency frequency;

    @NotNull(message = "Next payment date is required")
    private LocalDate nextPaymentDate;
}