package com.money.finance_tracker.repository;

import com.money.finance_tracker.entity.FundingSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FundingSourceRepository extends JpaRepository<FundingSource, Long> {
}

