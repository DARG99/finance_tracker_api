package com.money.finance_tracker.dto;

import com.money.finance_tracker.entity.TransactionTypeEnum;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

    @NotNull(message = "Transaction type is required")
    private TransactionTypeEnum type;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    private Long sourceFundingSourceId;

    private Long destinationFundingSourceId;

    private Long categoryId;

    private String description;

    private LocalDate transactionDate;

    @AssertTrue(message = "Invalid funding-source or category combination for this transaction type")
    public boolean isMovementValid() {
        if (type == null) {
            return true; // Let @NotNull return the proper error message
        }

        return switch (type) {
            case EXPENSE ->
                    sourceFundingSourceId != null
                            && destinationFundingSourceId == null
                            && categoryId != null;

            case INCOME ->
                    sourceFundingSourceId == null
                            && destinationFundingSourceId != null
                            && categoryId == null;

            case TRANSFER ->
                    sourceFundingSourceId != null
                            && destinationFundingSourceId != null
                            && !sourceFundingSourceId.equals(destinationFundingSourceId)
                            && categoryId == null;
        };
    }
}
