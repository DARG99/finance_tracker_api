package com.money.finance_tracker.repository;

import com.money.finance_tracker.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {
            "sourceFundingSource",
            "destinationFundingSource",
            "category"
    })
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
}