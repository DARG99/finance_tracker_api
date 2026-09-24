package com.money.finance_tracker.repository;

import com.money.finance_tracker.entity.Subscription;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    @EntityGraph(attributePaths = {
            "fundingSource",
            "category"
    })
    List<Subscription> findAllByUserIdOrderByNextPaymentDateAsc(
            Long userId
    );

    @EntityGraph(attributePaths = {
            "fundingSource",
            "category"
    })
    Optional<Subscription> findByIdAndUserId(
            Long subscriptionId,
            Long userId
    );

    @Query("""
        SELECT s.id
        FROM Subscription s
        WHERE s.active = true
          AND s.nextPaymentDate <= :today
        ORDER BY s.nextPaymentDate
        """)
    List<Long> findDueSubscriptionIds(
            @Param("today") LocalDate today
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "user",
            "fundingSource",
            "category"
    })
    @Query("""
        SELECT s
        FROM Subscription s
        WHERE s.id = :subscriptionId
        """)
    Optional<Subscription> findByIdForUpdate(
            @Param("subscriptionId") Long subscriptionId
    );
}