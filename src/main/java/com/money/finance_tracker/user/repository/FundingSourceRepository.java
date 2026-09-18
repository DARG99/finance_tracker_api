package com.money.finance_tracker.user.repository;

import com.money.finance_tracker.user.entity.FundingSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FundingSourceRepository extends JpaRepository<FundingSource, Long> {
}

