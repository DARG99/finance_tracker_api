package com.money.finance_tracker.service;

import com.money.finance_tracker.dto.ForecastResponseDto;
import com.money.finance_tracker.entity.*;
import com.money.finance_tracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ForecastService {
    public enum Horizon { THIS_MONTH, NEXT_MONTH }
    private record Occurrence(Long subscriptionId, LocalDate date) {}
    private final AccountLedgerService ledger;
    private final AccountClock clock;
    private final FundingSourceRepository sources;
    private final TransactionRepository transactions;
    private final SubscriptionRepository subscriptions;
    private final SubscriptionPaymentRepository payments;

    @Transactional
    public ForecastResponseDto forecast(User principal, Horizon horizon) {
        User user = ledger.reconcile(principal.getId());
        LocalDate today = clock.today(user);
        LocalDate through = YearMonth.from(today).plusMonths(horizon == Horizon.THIS_MONTH ? 0 : 1).atEndOfMonth();
        Map<Long, BigDecimal> scheduled = new HashMap<>();
        Map<Long, BigDecimal> recurring = new HashMap<>();
        Set<Occurrence> represented = new HashSet<>();
        for (Transaction transaction : transactions.findByUserIdAndStatus(user.getId(), TransactionStatus.SCHEDULED)) {
            Subscription subscription = transaction.getSubscription();
            if (subscription != null) {
                // A materialized occurrence replaces its virtual recurrence, even outside this horizon.
                represented.add(new Occurrence(subscription.getId(), transaction.getOccurrenceDate()));
                if (!subscription.isActive() || payments.existsBySubscriptionIdAndScheduledFor(
                        subscription.getId(), transaction.getOccurrenceDate())) continue;
            }
            if (transaction.getTransactionDate().isAfter(through)) continue;
            (subscription == null ? scheduled : recurring).merge(transaction.getSourceFundingSource().getId(),
                    transaction.getAmount(), BigDecimal::add);
        }
        for (Subscription subscription : subscriptions.findAllByUserIdOrderByNextPaymentDateAsc(user.getId())) {
            if (!subscription.isActive()) continue;
            for (LocalDate due = subscription.getNextPaymentDate(); !due.isAfter(through);
                 due = SubscriptionRecurrence.next(subscription, due)) {
                if (represented.contains(new Occurrence(subscription.getId(), due))
                        || payments.existsBySubscriptionIdAndScheduledFor(subscription.getId(), due)) continue;
                recurring.merge(subscription.getFundingSource().getId(), subscription.getAmount(), BigDecimal::add);
            }
        }
        return new ForecastResponseDto(today, through,
                sources.findAllByUserIdOrderByNameAsc(user.getId()).stream().map(source -> {
                    BigDecimal manual = scheduled.getOrDefault(source.getId(), BigDecimal.ZERO);
                    BigDecimal subscription = recurring.getOrDefault(source.getId(), BigDecimal.ZERO);
                    return new ForecastResponseDto.FundingSourceForecast(source.getId(), manual, subscription,
                            source.getBalance().subtract(manual).subtract(subscription));
                }).toList());
    }
}
