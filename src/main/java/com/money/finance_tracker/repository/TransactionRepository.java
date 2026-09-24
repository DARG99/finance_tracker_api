package com.money.finance_tracker.repository;

import com.money.finance_tracker.dto.CategorySpendingDto;
import com.money.finance_tracker.dto.MonthlySpendingDto;
import com.money.finance_tracker.entity.Transaction;
import com.money.finance_tracker.entity.TransactionTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends
        JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {
            "sourceFundingSource",
            "destinationFundingSource",
            "category"
    })
    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user.id = :userId
          AND t.type = :type
        """)
    BigDecimal sumAmountByUserAndType(
            Long userId,
            TransactionTypeEnum type
    );

    @Query("""
    SELECT new com.money.finance_tracker.dto.MonthlySpendingDto(
        MONTH(t.transactionDate),
        SUM(t.amount)
    )
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.type = :type
      AND t.transactionDate >= :startDate
      AND t.transactionDate < :endDate
    GROUP BY MONTH(t.transactionDate)
    ORDER BY MONTH(t.transactionDate)
    """)
    List<MonthlySpendingDto> getMonthlySpending(
            @Param("userId") Long userId,
            @Param("type") TransactionTypeEnum type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT new com.money.finance_tracker.dto.CategorySpendingDto(
        c.id,
        c.name,
        SUM(t.amount)
    )
    FROM Transaction t
    JOIN t.category c
    WHERE t.user.id = :userId
      AND t.type = :type
      AND t.transactionDate >= :startDate
      AND t.transactionDate < :endDate
    GROUP BY c.id, c.name
    ORDER BY SUM(t.amount) DESC
    """)
    List<CategorySpendingDto> getSpendingByCategory(
            @Param("userId") Long userId,
            @Param("type") TransactionTypeEnum type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    DELETE FROM Transaction t
    WHERE t.user.id = :userId
    """)
    int deleteAllByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT COUNT(t) > 0
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.category.id = :categoryId
""")
    boolean existsByCategoryIdAndUserId(
            Long categoryId,
            Long userId
    );

    @Query("""
    SELECT COUNT(t) > 0
    FROM Transaction t
    WHERE t.user.id = :userId
      AND (
            t.sourceFundingSource.id = :fundingSourceId
            OR t.destinationFundingSource.id = :fundingSourceId
      )
""")
    boolean existsByFundingSourceIdAndUserId(
            Long fundingSourceId,
            Long userId
    );
}