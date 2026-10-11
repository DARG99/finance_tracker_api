package com.money.finance_tracker.service;

import com.money.finance_tracker.entity.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SubscriptionRecurrenceTest {
    @ParameterizedTest
    @CsvSource({
        "MONTHLY,31,2026-01-31,2026-02-28",
        "MONTHLY,31,2026-02-28,2026-03-31",
        "MONTHLY,31,2027-12-31,2028-01-31",
        "MONTHLY,31,2028-01-31,2028-02-29",
        "MONTHLY,31,2028-02-29,2028-03-31",
        "YEARLY,29,2028-02-29,2029-02-28",
        "YEARLY,29,2031-02-28,2032-02-29"
    })
    void preservesAnchor(SubscriptionFrequency frequency, short day, LocalDate current, LocalDate expected) {
        Subscription subscription = new Subscription();
        subscription.setFrequency(frequency); subscription.setBillingDay(day);
        assertEquals(expected, SubscriptionRecurrence.next(subscription, current));
    }
}
