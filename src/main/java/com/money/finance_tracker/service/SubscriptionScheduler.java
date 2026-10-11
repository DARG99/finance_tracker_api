package com.money.finance_tracker.service;

import com.money.finance_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {
    private final UserRepository users;
    private final AccountLedgerService ledger;

    // Runs after startup and retries missed dates; each account commits independently.
    @Scheduled(fixedDelayString = "${finance.posting-delay-ms:60000}")
    public void processDueSubscriptions() {
        for (Long id : users.findAccountIds()) {
            try { ledger.reconcile(id); }
            catch (RuntimeException error) { log.error("Could not reconcile account {}", id, error); }
        }
    }
}
