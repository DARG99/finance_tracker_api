package com.money.finance_tracker.repository;

import com.money.finance_tracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    List<Transaction> findAllByUserIdOrderByTransactionDateDesc(Long userId);
}
