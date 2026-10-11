package com.money.finance_tracker.service;

import com.money.finance_tracker.entity.*;
import com.money.finance_tracker.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AccountLedgerService {
    private final UserRepository users;
    private final TransactionRepository transactions;
    private final SubscriptionRepository subscriptions;
    private final SubscriptionPaymentRepository payments;
    private final AccountClock clock;

    // Every ledger reader/writer takes this lock first, held until its transaction commits.
    @Transactional
    public User lock(Long userId) {
        return users.lockById(userId).orElseThrow(() -> new EntityNotFoundException("Account not found"));
    }

    @Transactional
    public User reconcile(Long userId) {
        User user = lock(userId);
        LocalDate today = clock.today(user);
        for (Subscription subscription : subscriptions.findAllByUserIdOrderByNextPaymentDateAsc(userId)) {
            if (!subscription.isActive()) { cancelGenerated(subscription); continue; }
            while (!subscription.getNextPaymentDate().isAfter(today)) {
                LocalDate due = subscription.getNextPaymentDate();
                Transaction generated = transactions.findBySubscriptionIdAndOccurrenceDate(subscription.getId(), due)
                        .orElse(null);
                if (!payments.existsBySubscriptionIdAndScheduledFor(subscription.getId(), due)) {
                    if (generated == null) {
                        generated = new Transaction();
                        generated.setUser(user);
                        generated.setType(TransactionTypeEnum.EXPENSE);
                        generated.setStatus(TransactionStatus.SCHEDULED);
                        generated.setAmount(subscription.getAmount());
                        generated.setSourceFundingSource(subscription.getFundingSource());
                        generated.setCategory(subscription.getCategory());
                        generated.setDescription(subscription.getName());
                        generated.setTransactionDate(due);
                        generated.setSubscription(subscription);
                        generated.setOccurrenceDate(due);
                        transactions.save(generated);
                    }
                    post(generated);
                } else if (generated != null && generated.getStatus() == TransactionStatus.SCHEDULED) {
                    transactions.delete(generated);
                }
                subscription.setNextPaymentDate(SubscriptionRecurrence.next(subscription, due));
            }
        }
        for (Transaction transaction : transactions.findByUserIdAndStatus(userId, TransactionStatus.SCHEDULED)) {
            if (transaction.getSubscription() != null && !transaction.getSubscription().isActive()) {
                transactions.delete(transaction);
            } else if (!transaction.getTransactionDate().isAfter(today)) {
                post(transaction);
            }
        }
        return user;
    }

    private void post(Transaction transaction) {
        if (transaction.getStatus() == TransactionStatus.POSTED) return;
        if (transaction.getSubscription() != null) {
            if (payments.existsBySubscriptionIdAndScheduledFor(
                    transaction.getSubscription().getId(), transaction.getOccurrenceDate())) {
                transactions.delete(transaction);
                return;
            }
            SubscriptionPayment payment = new SubscriptionPayment();
            payment.setSubscription(transaction.getSubscription());
            payment.setScheduledFor(transaction.getOccurrenceDate());
            payment.setTransaction(transaction);
            payment.setAmount(transaction.getAmount());
            payments.save(payment);
        }
        transaction.getSourceFundingSource().setBalance(transaction.getSourceFundingSource().getBalance().subtract(transaction.getAmount()));
        transaction.setStatus(TransactionStatus.POSTED);
    }

    public void cancelGenerated(Subscription subscription) {
        transactions.deleteAll(transactions.findBySubscriptionIdAndStatus(subscription.getId(), TransactionStatus.SCHEDULED));
    }
}
