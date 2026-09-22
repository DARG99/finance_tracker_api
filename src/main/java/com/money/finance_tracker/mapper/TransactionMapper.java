package com.money.finance_tracker.mapper;

import com.money.finance_tracker.dto.TransactionResponseDto;
import com.money.finance_tracker.entity.Transaction;
import org.springframework.stereotype.Component;

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

        return response;
    }
}