package com.money.finance_tracker.service;

import com.money.finance_tracker.dto.PageResponseDto;
import com.money.finance_tracker.dto.TransactionDto;
import com.money.finance_tracker.dto.TransactionResponseDto;
import com.money.finance_tracker.dto.TransactionUpdateDto;
import com.money.finance_tracker.entity.*;
import com.money.finance_tracker.mapper.TransactionMapper;
import com.money.finance_tracker.repository.CategoryRepository;
import com.money.finance_tracker.repository.FundingSourceRepository;
import com.money.finance_tracker.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FundingSourceRepository fundingSourceRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponseDto addTransaction(
            TransactionDto dto,
            User user
    ) {
        Transaction transaction = new Transaction();

        transaction.setUser(user);

        transaction.setType(dto.getType());
        transaction.setAmount(dto.getAmount());
        transaction.setDescription(dto.getDescription());
        transaction.setTransactionDate(dto.getTransactionDate());

        switch (dto.getType()) {
            case EXPENSE -> configureExpense(transaction, dto, user);
            case INCOME -> configureIncome(transaction, dto, user);
            case TRANSFER -> configureTransfer(transaction, dto, user);
        }

        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toResponseDto(savedTransaction);
    }

    @Transactional
    public Transaction createExpenseFromSubscription(
            Subscription subscription,
            LocalDate paymentDate
    ) {
        Transaction transaction = new Transaction();

        transaction.setUser(subscription.getUser());
        transaction.setType(TransactionTypeEnum.EXPENSE);
        transaction.setAmount(subscription.getAmount());

        transaction.setSourceFundingSource(
                subscription.getFundingSource()
        );

        transaction.setDestinationFundingSource(null);
        transaction.setCategory(subscription.getCategory());

        transaction.setDescription(subscription.getName());
        transaction.setTransactionDate(paymentDate);

        subscription.getFundingSource()
                .subtractFromBalance(subscription.getAmount());

        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<TransactionResponseDto> getTransactions(
            User user,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("transactionDate"),
                        Sort.Order.desc("id")
                )
        );

        Page<Transaction> transactionPage = transactionRepository
                .findByUserId(user.getId(), pageable);

        List<TransactionResponseDto> transactions = transactionPage
                .getContent()
                .stream()
                .map(transactionMapper::toResponseDto)
                .toList();

        return new PageResponseDto<>(
                transactions,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isFirst(),
                transactionPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public TransactionResponseDto getTransactionById(
            Long transactionId,
            User user
    ) {
        Transaction transaction = findTransaction(transactionId, user);

        return transactionMapper.toResponseDto(transaction);
    }


    @Transactional
    public void deleteTransaction(Long transactionId, User user) {
        Transaction transaction = findTransaction(transactionId, user);

        reverseTransactionEffect(transaction);

        transactionRepository.delete(transaction);
    }

    private void configureExpense(
            Transaction transaction,
            TransactionDto dto,
            User user
    ) {
        FundingSource source = findFundingSource(
                dto.getSourceFundingSourceId(), user
        );

        Category category = findCategory(dto.getCategoryId(), user);

        source.subtractFromBalance(dto.getAmount());

        transaction.setSourceFundingSource(source);
        transaction.setCategory(category);
    }

    private void configureIncome(
            Transaction transaction,
            TransactionDto dto,
            User user
    ) {
        FundingSource destination = findFundingSource(
                dto.getDestinationFundingSourceId(), user
        );

        destination.addToBalance(dto.getAmount());

        transaction.setDestinationFundingSource(destination);
    }

    private void configureTransfer(
            Transaction transaction,
            TransactionDto dto,
            User user
    ) {
        FundingSource source = findFundingSource(
                dto.getSourceFundingSourceId(), user
        );

        FundingSource destination = findFundingSource(
                dto.getDestinationFundingSourceId(), user
        );

        source.subtractFromBalance(dto.getAmount());
        destination.addToBalance(dto.getAmount());

        transaction.setSourceFundingSource(source);
        transaction.setDestinationFundingSource(destination);
    }

    private void reverseTransactionEffect(Transaction transaction) {
        switch (transaction.getType()) {
            case EXPENSE -> transaction.getSourceFundingSource()
                    .addToBalance(transaction.getAmount());

            case INCOME -> transaction.getDestinationFundingSource()
                    .subtractFromBalance(transaction.getAmount());

            case TRANSFER -> {
                transaction.getSourceFundingSource()
                        .addToBalance(transaction.getAmount());

                transaction.getDestinationFundingSource()
                        .subtractFromBalance(transaction.getAmount());
            }
        }
    }

    private Transaction findTransaction(Long transactionId, User user) {
        return transactionRepository
                .findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Transaction not found"));
    }

    private FundingSource findFundingSource(
            Long fundingSourceId,
            User user
    ) {
        return fundingSourceRepository
                .findByIdAndUserId(fundingSourceId, user.getId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Funding source not found"));
    }

    private Category findCategory(
            Long categoryId,
            User user
    ) {
        return categoryRepository
                .findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Category not found"));
    }

    @Transactional
    public TransactionResponseDto patchTransaction(
            Long id,
            TransactionUpdateDto dto,
            User user
    ) {
        Transaction transaction = findTransaction(id, user);

        switch (transaction.getType()) {
            case EXPENSE -> patchExpense(transaction, dto, user);
            case INCOME -> patchIncome(transaction, dto, user);
            case TRANSFER -> patchTransfer(transaction, dto, user);
        }

        return transactionMapper.toResponseDto(transaction);
    }

    private void patchExpense(
            Transaction transaction,
            TransactionUpdateDto dto,
            User user
    ) {
        FundingSource newSource = null;
        Category newCategory = null;

        if (dto.getSourceFundingSourceId() != null) {
            newSource = findFundingSource(
                    dto.getSourceFundingSourceId(), user
            );
        }

        if (dto.getCategoryId() != null) {
            newCategory = findCategory(dto.getCategoryId(), user);
        }

        boolean balanceChanged =
                dto.getAmount() != null || newSource != null;

        if (balanceChanged) {
            reverseTransactionEffect(transaction);
        }

        patchCommonFields(transaction, dto);

        if (newSource != null) {
            transaction.setSourceFundingSource(newSource);
        }

        if (newCategory != null) {
            transaction.setCategory(newCategory);
        }

        if (balanceChanged) {
            transaction.getSourceFundingSource()
                    .subtractFromBalance(transaction.getAmount());
        }
    }

    private void patchIncome(
            Transaction transaction,
            TransactionUpdateDto dto,
            User user
    ) {
        if (dto.getSourceFundingSourceId() != null
                || dto.getCategoryId() != null) {
            throw new IllegalArgumentException(
                    "Income can only use a destination funding source"
            );
        }

        FundingSource newDestination = null;

        if (dto.getDestinationFundingSourceId() != null) {
            newDestination = findFundingSource(
                    dto.getDestinationFundingSourceId(), user
            );
        }

        boolean balanceChanged =
                dto.getAmount() != null || newDestination != null;

        if (balanceChanged) {
            reverseTransactionEffect(transaction);
        }

        patchCommonFields(transaction, dto);

        if (newDestination != null) {
            transaction.setDestinationFundingSource(newDestination);
        }

        if (balanceChanged) {
            transaction.getDestinationFundingSource()
                    .addToBalance(transaction.getAmount());
        }
    }

    private void patchTransfer(
            Transaction transaction,
            TransactionUpdateDto dto,
            User user
    ) {
        if (dto.getCategoryId() != null) {
            throw new IllegalArgumentException(
                    "A transfer cannot have a category"
            );
        }

        FundingSource newSource = null;
        FundingSource newDestination = null;

        if (dto.getSourceFundingSourceId() != null) {
            newSource = findFundingSource(
                    dto.getSourceFundingSourceId(), user
            );
        }

        if (dto.getDestinationFundingSourceId() != null) {
            newDestination = findFundingSource(
                    dto.getDestinationFundingSourceId(), user
            );
        }

        FundingSource finalSource = newSource != null
                ? newSource
                : transaction.getSourceFundingSource();

        FundingSource finalDestination = newDestination != null
                ? newDestination
                : transaction.getDestinationFundingSource();

        if (finalSource.getId().equals(finalDestination.getId())) {
            throw new IllegalArgumentException(
                    "Transfer source and destination must be different"
            );
        }

        boolean balanceChanged =
                dto.getAmount() != null
                        || newSource != null
                        || newDestination != null;

        if (balanceChanged) {
            reverseTransactionEffect(transaction);
        }

        patchCommonFields(transaction, dto);

        if (newSource != null) {
            transaction.setSourceFundingSource(newSource);
        }

        if (newDestination != null) {
            transaction.setDestinationFundingSource(newDestination);
        }

        if (balanceChanged) {
            transaction.getSourceFundingSource()
                    .subtractFromBalance(transaction.getAmount());

            transaction.getDestinationFundingSource()
                    .addToBalance(transaction.getAmount());
        }
    }

    private void patchCommonFields(
            Transaction transaction,
            TransactionUpdateDto dto
    ) {
        if (dto.getAmount() != null) {
            transaction.setAmount(dto.getAmount());
        }

        if (dto.getTransactionDate() != null) {
            transaction.setTransactionDate(dto.getTransactionDate());
        }

        if (dto.getDescription() != null) {
            transaction.setDescription(dto.getDescription());
        }
    }
}