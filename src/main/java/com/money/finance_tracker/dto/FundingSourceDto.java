package com.money.finance_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundingSourceDto {

    @NotBlank(message = "Funding source name is required")
    private String name;

    private BigDecimal initialBalance;
}
