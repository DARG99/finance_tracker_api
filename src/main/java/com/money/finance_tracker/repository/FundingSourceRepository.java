package com.money.finance_tracker.repository;

import com.money.finance_tracker.entity.FundingSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FundingSourceRepository extends JpaRepository<FundingSource, Long> {
    Optional<FundingSource> findByIdAndUserId(Long fundingSourceId, Long id);
}

