package com.money.finance_tracker.service;

import java.time.LocalDate;
import java.time.YearMonth;

public final class PaydaySchedule {
    private PaydaySchedule() {}

    public static LocalDate nextPayday(LocalDate today, int paydayDay) {
        if (paydayDay < 1 || paydayDay > 31) {
            throw new IllegalArgumentException("Payday must be a day between 1 and 31");
        }
        YearMonth month = YearMonth.from(today);
        LocalDate payday = inMonth(month, paydayDay);
        // On payday the new cycle starts. Actual income still has to be posted.
        return payday.isAfter(today) ? payday : inMonth(month.plusMonths(1), paydayDay);
    }

    private static LocalDate inMonth(YearMonth month, int day) {
        return month.atDay(Math.min(day, month.lengthOfMonth()));
    }
}
