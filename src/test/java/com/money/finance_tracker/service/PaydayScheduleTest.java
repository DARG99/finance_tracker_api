package com.money.finance_tracker.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PaydayScheduleTest {
    @ParameterizedTest
    @CsvSource({
            "2026-10-11,25,2026-10-25",
            "2026-10-24,25,2026-10-25",
            "2026-10-25,25,2026-11-25",
            "2026-10-26,25,2026-11-25",
            "2026-12-25,25,2027-01-25",
            "2026-02-01,31,2026-02-28",
            "2026-02-28,31,2026-03-31",
            "2028-02-01,31,2028-02-29",
            "2028-02-29,31,2028-03-31",
            "2026-10-31,1,2026-11-01"
    })
    void findsNextPaydayWithoutLosingTheDayAnchor(LocalDate today, int day, LocalDate expected) {
        assertEquals(expected, PaydaySchedule.nextPayday(today, day));
    }
}
