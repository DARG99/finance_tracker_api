package com.money.finance_tracker.mapper;

import com.money.finance_tracker.dto.ReimbursableExpenseResponseDto;
import com.money.finance_tracker.dto.TransactionResponseDto;
import com.money.finance_tracker.entity.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionMapper {

    public TransactionResponseDto toResponseDto(Transaction transaction) {
        TransactionResponseDto response = new TransactionResponseDto();

        response.setId(transaction.getId());
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());
        response.setDescription(transaction.getDescription());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setCreatedAt(transaction.getCreatedAt());

        if (transaction.getSourceFundingSource() != null) {
            response.setSourceFundingSourceId(
                    transaction.getSourceFundingSource().getId()
            );
            response.setSourceFundingSourceName(
                    transaction.getSourceFundingSource().getName()
            );
        }

        if (transaction.getDestinationFundingSource() != null) {
            response.setDestinationFundingSourceId(
                    transaction.getDestinationFundingSource().getId()
            );
            response.setDestinationFundingSourceName(
                    transaction.getDestinationFundingSource().getName()
            );
        }

        if (transaction.getCategory() != null) {
            response.setCategoryId(transaction.getCategory().getId());
            response.setCategoryName(transaction.getCategory().getName());
        }

        response.setTransactionNature(
                transaction.getTransactionNature()
        );

        if (transaction.getReimbursementForTransaction() != null) {
            response.setReimbursementForTransactionId(
                    transaction.getReimbursementForTransaction().getId()
            );

            response.setReimbursementForDescription(
                    transaction.getReimbursementForTransaction().getDescription()
            );
        }

        return response;
    }

    public ReimbursableExpenseResponseDto toReimbursableExpenseResponseDto(
            Transaction expense,
            BigDecimal reimbursedAmount
    ) {
        ReimbursableExpenseResponseDto response =
                new ReimbursableExpenseResponseDto();

        response.setId(expense.getId());
        response.setDescription(expense.getDescription());
        response.setTransactionDate(expense.getTransactionDate());
        response.setAmount(expense.getAmount());
        response.setAlreadyReimbursedAmount(reimbursedAmount);

        response.setRemainingReimbursableAmount(
                expense.getAmount()
                        .subtract(reimbursedAmount)
                        .max(BigDecimal.ZERO)
        );

        if (expense.getCategory() != null) {
            response.setCategoryName(expense.getCategory().getName());
        }

        if (expense.getSourceFundingSource() != null) {
            response.setSourceFundingSourceName(
                    expense.getSourceFundingSource().getName()
            );
        }

        return response;
    }
}