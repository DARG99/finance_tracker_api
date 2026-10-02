package com.money.finance_tracker.dto;

import com.money.finance_tracker.entity.TransactionNature;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionUpdateDto {

    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    private Long sourceFundingSourceId;
    private Long destinationFundingSourceId;
    private Long categoryId;

    private TransactionNature transactionNature;
    private Long reimbursementForTransactionId;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean reimbursementForTransactionIdPresent;

    public void setReimbursementForTransactionId(Long reimbursementForTransactionId) {
        this.reimbursementForTransactionId = reimbursementForTransactionId;
        this.reimbursementForTransactionIdPresent = true;
    }

    public boolean hasReimbursementForTransactionId() {
        return reimbursementForTransactionIdPresent;
    }

    private String description;
    private LocalDate transactionDate;
}
