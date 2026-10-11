package com.money.finance_tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ForecastResponseDto(LocalDate asOf, LocalDate through, List<FundingSourceForecast> fundingSources) {
    public record FundingSourceForecast(Long id, BigDecimal scheduledExpenses,
                                        BigDecimal subscriptionExpenses, BigDecimal predictedBalance) {}
}
