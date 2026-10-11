package com.money.finance_tracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaydaySettingsDto(
        @NotNull(message = "Payday is required")
        @Min(value = 1, message = "Payday must be between 1 and 31")
        @Max(value = 31, message = "Payday must be between 1 and 31")
        Integer paydayDay) {}
