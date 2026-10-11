package com.money.finance_tracker.service;

import com.money.finance_tracker.entity.*;
import java.time.LocalDate;

public final class SubscriptionRecurrence {
    private SubscriptionRecurrence() {}
    // February 29 anniversaries clamp to February 28 and return to 29 in leap years.
    public static LocalDate next(Subscription subscription, LocalDate current) {
        LocalDate month = current.withDayOfMonth(1);
        month = subscription.getFrequency() == SubscriptionFrequency.MONTHLY
                ? month.plusMonths(1) : month.plusYears(1);
        return month.withDayOfMonth(Math.min(subscription.getBillingDay(), month.lengthOfMonth()));
    }
}
