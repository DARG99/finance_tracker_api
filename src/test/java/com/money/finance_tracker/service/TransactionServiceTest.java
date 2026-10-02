package com.money.finance_tracker.service;

import com.money.finance_tracker.dto.TransactionUpdateDto;
import com.money.finance_tracker.dto.TransactionDto;
import tools.jackson.databind.json.JsonMapper;
import com.money.finance_tracker.entity.FundingSource;
import com.money.finance_tracker.entity.Transaction;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.mapper.TransactionMapper;
import com.money.finance_tracker.repository.CategoryRepository;
import com.money.finance_tracker.repository.FundingSourceRepository;
import com.money.finance_tracker.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static com.money.finance_tracker.entity.TransactionNature.*;
import static com.money.finance_tracker.entity.TransactionTypeEnum.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {
    private TransactionRepository repository;
    private TransactionService service;
    private FundingSourceRepository fundingSources;
    private User user;
    private Transaction income;
    private Transaction expense;
    private FundingSource account;

    @BeforeEach
    void setUp() {
        repository = mock(TransactionRepository.class);
        fundingSources = mock(FundingSourceRepository.class);
        service = new TransactionService(repository, fundingSources,
                mock(CategoryRepository.class), new TransactionMapper());
        user = new User();
        user.setId(1L);
        account = new FundingSource();
        account.setBalance(new BigDecimal("425"));
        income = new Transaction();
        income.setId(2L);
        income.setUser(user);
        income.setType(INCOME);
        income.setTransactionNature(NORMAL);
        income.setAmount(new BigDecimal("25"));
        income.setDestinationFundingSource(account);
        expense = new Transaction();
        expense.setId(3L);
        expense.setUser(user);
        expense.setType(EXPENSE);
        expense.setAmount(new BigDecimal("100"));
        when(repository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(income));
        when(repository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(expense));
        when(repository.getReimbursedAmountForExpense(3L, 1L, INCOME, REIMBURSEMENT))
                .thenReturn(BigDecimal.ZERO);
    }

    private TransactionUpdateDto conversion() {
        TransactionUpdateDto dto = new TransactionUpdateDto();
        dto.setTransactionNature(REIMBURSEMENT);
        dto.setReimbursementForTransactionId(3L);
        return dto;
    }

    @Test
    void convertsIncomeWithoutChangingBalanceOrAmount() {
        var response = service.patchTransaction(2L, conversion(), user);
        assertEquals(REIMBURSEMENT, response.getTransactionNature());
        assertEquals(3L, response.getReimbursementForTransactionId());
        assertEquals(new BigDecimal("25"), response.getAmount());
        assertEquals(new BigDecimal("425"), account.getBalance());
    }

    @Test
    void marksIncomeAsReimbursementWithoutAnExpense() {
        TransactionUpdateDto dto = new TransactionUpdateDto();
        dto.setTransactionNature(REIMBURSEMENT);
        var response = service.patchTransaction(2L, dto, user);
        assertEquals(REIMBURSEMENT, response.getTransactionNature());
        assertNull(response.getReimbursementForTransactionId());
        assertEquals(new BigDecimal("425"), account.getBalance());
        verify(repository, never()).getReimbursedAmountForExpense(any(), any(), any(), any());
    }

    @Test
    void createsStandaloneReimbursementAndCreditsAccount() {
        when(fundingSources.findByIdAndUserId(4L, 1L)).thenReturn(Optional.of(account));
        when(repository.save(any(Transaction.class))).thenAnswer(call -> call.getArgument(0));
        TransactionDto dto = new TransactionDto();
        dto.setType(INCOME);
        dto.setTransactionNature(REIMBURSEMENT);
        dto.setAmount(new BigDecimal("250"));
        dto.setDestinationFundingSourceId(4L);
        var response = service.addTransaction(dto, user);
        assertEquals(REIMBURSEMENT, response.getTransactionNature());
        assertNull(response.getReimbursementForTransactionId());
        assertEquals(new BigDecimal("675"), account.getBalance());
        verify(repository, never()).getReimbursedAmountForExpense(any(), any(), any(), any());
    }

    @Test
    void explicitNullUnlinksExpenseWhileOmissionPreservesIt() {
        income.setTransactionNature(REIMBURSEMENT);
        income.setReimbursementForTransaction(expense);
        JsonMapper json = JsonMapper.builder().build();
        service.patchTransaction(2L, json.readValue(
                "{\"description\":\"Combined refund\"}", TransactionUpdateDto.class), user);
        assertSame(expense, income.getReimbursementForTransaction());
        service.patchTransaction(2L, json.readValue(
                "{\"reimbursementForTransactionId\":null}", TransactionUpdateDto.class), user);
        assertNull(income.getReimbursementForTransaction());
        assertEquals(REIMBURSEMENT, income.getTransactionNature());
        assertEquals(new BigDecimal("425"), account.getBalance());
    }

    @Test
    void editsStandaloneReimbursementAmount() {
        income.setTransactionNature(REIMBURSEMENT);
        TransactionUpdateDto dto = new TransactionUpdateDto();
        dto.setAmount(new BigDecimal("250"));
        service.patchTransaction(2L, dto, user);
        assertEquals(new BigDecimal("650"), account.getBalance());
        assertNull(income.getReimbursementForTransaction());
        assertEquals(REIMBURSEMENT, income.getTransactionNature());
    }

    @Test
    void rejectsAnotherUsersExpense() {
        when(repository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> service.patchTransaction(2L, conversion(), user));
    }

    @Test
    void rejectsLinkToIncome() {
        expense.setType(INCOME);
        assertThrows(IllegalArgumentException.class,
                () -> service.patchTransaction(2L, conversion(), user));
    }

    @Test
    void rejectsConversionExceedingRemainingExpense() {
        when(repository.getReimbursedAmountForExpense(3L, 1L, INCOME, REIMBURSEMENT))
                .thenReturn(new BigDecimal("80"));
        assertThrows(IllegalArgumentException.class,
                () -> service.patchTransaction(2L, conversion(), user));
        assertEquals(NORMAL, income.getTransactionNature());
        assertEquals(new BigDecimal("425"), account.getBalance());
    }

    @Test
    void editsExistingReimbursementWithoutCountingItTwice() {
        income.setTransactionNature(REIMBURSEMENT);
        income.setReimbursementForTransaction(expense);
        when(repository.getReimbursedAmountForExpense(3L, 1L, INCOME, REIMBURSEMENT))
                .thenReturn(new BigDecimal("25"));
        TransactionUpdateDto dto = new TransactionUpdateDto();
        dto.setAmount(new BigDecimal("100"));
        service.patchTransaction(2L, dto, user);
        assertEquals(new BigDecimal("100"), income.getAmount());
        assertEquals(new BigDecimal("500"), account.getBalance());
        assertSame(expense, income.getReimbursementForTransaction());
    }

    @Test
    void rejectsExcessiveAmountWhenNatureIsOmitted() {
        income.setTransactionNature(REIMBURSEMENT);
        income.setReimbursementForTransaction(expense);
        when(repository.getReimbursedAmountForExpense(3L, 1L, INCOME, REIMBURSEMENT))
                .thenReturn(new BigDecimal("25"));
        TransactionUpdateDto dto = new TransactionUpdateDto();
        dto.setAmount(new BigDecimal("101"));
        assertThrows(IllegalArgumentException.class,
                () -> service.patchTransaction(2L, dto, user));
        assertEquals(new BigDecimal("25"), income.getAmount());
        assertEquals(new BigDecimal("425"), account.getBalance());
    }

    @Test
    void canConvertBackToNormalIncome() {
        income.setTransactionNature(REIMBURSEMENT);
        income.setReimbursementForTransaction(expense);
        TransactionUpdateDto dto = new TransactionUpdateDto();
        dto.setTransactionNature(NORMAL);
        service.patchTransaction(2L, dto, user);
        assertEquals(NORMAL, income.getTransactionNature());
        assertNull(income.getReimbursementForTransaction());
        assertEquals(new BigDecimal("425"), account.getBalance());
    }

    @Test
    void cannotConvertAnExpenseToReimbursement() {
        income.setType(EXPENSE);
        assertThrows(IllegalArgumentException.class,
                () -> service.patchTransaction(2L, conversion(), user));
    }
}
