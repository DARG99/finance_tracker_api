package com.money.finance_tracker.service;

import com.money.finance_tracker.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private static final ZoneId PORTUGAL_ZONE =
            ZoneId.of("Europe/Lisbon");

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionChargeService subscriptionChargeService;

    @Scheduled(
            cron = "0 5 0 * * *",
            zone = "Europe/Lisbon"
    )
    public void processDueSubscriptions() {
        LocalDate today = LocalDate.now(PORTUGAL_ZONE);

        List<Long> dueSubscriptionIds =
                subscriptionRepository.findDueSubscriptionIds(today);

        for (Long subscriptionId : dueSubscriptionIds) {
            try {
                // Each subscription has its own database transaction.
                subscriptionChargeService.chargeIfDue(
                        subscriptionId,
                        today
                );
            } catch (RuntimeException exception) {
                log.error(
                        "Could not charge subscription {}",
                        subscriptionId,
                        exception
                );
            }
        }
    }
}