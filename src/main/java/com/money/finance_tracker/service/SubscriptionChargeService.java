package com.money.finance_tracker.service;

import com.money.finance_tracker.entity.*;
import com.money.finance_tracker.repository.SubscriptionPaymentRepository;
import com.money.finance_tracker.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SubscriptionChargeService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final TransactionService transactionService;

    @Transactional
    public void chargeIfDue(
            Long subscriptionId,
            LocalDate today
    ) {
        Subscription subscription = subscriptionRepository
                .findByIdForUpdate(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subscription not found"
                ));

        if (!subscription.isActive()
                || subscription.getNextPaymentDate().isAfter(today)) {
            return;
        }

        // Charges missed dates too, for example while the Pi was offline.
        while (!subscription.getNextPaymentDate().isAfter(today)) {
            LocalDate scheduledFor =
                    subscription.getNextPaymentDate();

            boolean alreadyCharged =
                    subscriptionPaymentRepository
                            .existsBySubscriptionIdAndScheduledFor(
                                    subscription.getId(),
                                    scheduledFor
                            );

            if (!alreadyCharged) {
                Transaction transaction =
                        transactionService.createExpenseFromSubscription(
                                subscription,
                                scheduledFor
                        );

                SubscriptionPayment payment =
                        new SubscriptionPayment();

                payment.setSubscription(subscription);
                payment.setTransaction(transaction);
                payment.setScheduledFor(scheduledFor);
                payment.setAmount(subscription.getAmount());

                subscriptionPaymentRepository.save(payment);
            }

            subscription.setNextPaymentDate(
                    calculateNextPaymentDate(
                            subscription,
                            scheduledFor
                    )
            );
        }
    }

    private LocalDate calculateNextPaymentDate(
            Subscription subscription,
            LocalDate currentDate
    ) {
        if (subscription.getFrequency()
                == SubscriptionFrequency.MONTHLY) {

            LocalDate nextMonth = currentDate
                    .withDayOfMonth(1)
                    .plusMonths(1);

            int day = Math.min(
                    subscription.getBillingDay(),
                    nextMonth.lengthOfMonth()
            );

            return nextMonth.withDayOfMonth(day);
        }

        LocalDate nextYear = currentDate.plusYears(1);

        int day = Math.min(
                subscription.getBillingDay(),
                nextYear.lengthOfMonth()
        );

        return nextYear.withDayOfMonth(day);
    }
}