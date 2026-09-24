package com.money.finance_tracker.repository;

import com.money.finance_tracker.entity.FundingSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FundingSourceRepository extends JpaRepository<FundingSource, Long> {
    Optional<FundingSource> findByIdAndUserId(Long fundingSourceId, Long id);
    List<FundingSource> findAllByUserIdOrderByNameAsc(Long userId);

    @Query("""
    SELECT COALESCE(SUM(f.balance), 0)
    FROM FundingSource f
    WHERE f.user.id = :userId
    """)
    BigDecimal sumBalancesByUserId(Long userId);



}

