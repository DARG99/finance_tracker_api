package com.money.finance_tracker.repository;

import com.money.finance_tracker.dto.CategorySpendingDto;
import com.money.finance_tracker.dto.MonthlySpendingDto;
import com.money.finance_tracker.entity.Transaction;
import com.money.finance_tracker.entity.TransactionNature;
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
            @Param("userId") Long userId,
            @Param("type") TransactionTypeEnum type
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

    // Attribute refunds to the original expense month, even across years.
    @Query("""
    SELECT new com.money.finance_tracker.dto.MonthlySpendingDto(
        MONTH(e.transactionDate),
        SUM(r.amount)
    )
    FROM Transaction r
    JOIN r.reimbursementForTransaction e
    WHERE r.user.id = :userId
      AND e.user.id = :userId
      AND r.type = :incomeType
      AND r.transactionNature = :reimbursementNature
      AND e.type = :expenseType
      AND e.transactionDate >= :startDate
      AND e.transactionDate < :endDate
    GROUP BY MONTH(e.transactionDate)
    ORDER BY MONTH(e.transactionDate)
    """)
    List<MonthlySpendingDto> getMonthlyReimbursementsForExpenses(
            @Param("userId") Long userId,
            @Param("incomeType") TransactionTypeEnum incomeType,
            @Param("reimbursementNature") TransactionNature reimbursementNature,
            @Param("expenseType") TransactionTypeEnum expenseType,
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

    // Use the original expense's category and year, regardless of refund date.
    @Query("""
    SELECT new com.money.finance_tracker.dto.CategorySpendingDto(
        c.id,
        c.name,
        SUM(r.amount)
    )
    FROM Transaction r
    JOIN r.reimbursementForTransaction e
    JOIN e.category c
    WHERE r.user.id = :userId
      AND e.user.id = :userId
      AND r.type = :incomeType
      AND r.transactionNature = :reimbursementNature
      AND e.type = :expenseType
      AND e.transactionDate >= :startDate
      AND e.transactionDate < :endDate
    GROUP BY c.id, c.name
    """)
    List<CategorySpendingDto> getReimbursementsByExpenseCategory(
            @Param("userId") Long userId,
            @Param("incomeType") TransactionTypeEnum incomeType,
            @Param("reimbursementNature") TransactionNature reimbursementNature,
            @Param("expenseType") TransactionTypeEnum expenseType,
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

    @Query("""
    SELECT e
    FROM Transaction e
    WHERE e.user.id = :userId
      AND e.type = :expenseType
      AND (
          :search = ''
          OR LOWER(COALESCE(e.description, ''))
             LIKE LOWER(CONCAT('%', :search, '%'))
      )
      AND e.amount > COALESCE((
          SELECT SUM(r.amount)
          FROM Transaction r
          WHERE r.reimbursementForTransaction = e
            AND r.user.id = :userId
            AND r.type = :incomeType
            AND r.transactionNature = :reimbursementNature
      ), 0)
    """)
    Page<Transaction> findReimbursableExpenses(
            @Param("userId") Long userId,
            @Param("expenseType") TransactionTypeEnum expenseType,
            @Param("incomeType") TransactionTypeEnum incomeType,
            @Param("reimbursementNature")
            TransactionNature reimbursementNature,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
    SELECT COALESCE(SUM(r.amount), 0)
    FROM Transaction r
    WHERE r.reimbursementForTransaction.id = :expenseId
      AND r.user.id = :userId
      AND r.type = :incomeType
      AND r.transactionNature = :reimbursementNature
    """)
    BigDecimal getReimbursedAmountForExpense(
            @Param("expenseId") Long expenseId,
            @Param("userId") Long userId,
            @Param("incomeType") TransactionTypeEnum incomeType,
            @Param("reimbursementNature")
            TransactionNature reimbursementNature
    );

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.type = :type
      AND t.transactionNature = :transactionNature
    """)
    BigDecimal getTotalByTypeAndNature(
            @Param("userId") Long userId,
            @Param("type") TransactionTypeEnum type,
            @Param("transactionNature")
            TransactionNature transactionNature
    );

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.type = :type
    """)
    BigDecimal getTotalByType(
            @Param("userId") Long userId,
            @Param("type") TransactionTypeEnum type
    );
}
