package com.money.finance_tracker.repository;

import com.money.finance_tracker.entity.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface SubscriptionPaymentRepository
        extends JpaRepository<SubscriptionPayment, Long> {

    boolean existsBySubscriptionIdAndScheduledFor(
            Long subscriptionId,
            LocalDate scheduledFor
    );
}