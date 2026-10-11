package com.money.finance_tracker.service;

import com.money.finance_tracker.entity.User;
import org.springframework.stereotype.Component;
import java.time.*;

@Component
public class AccountClock {
    private final Clock clock;
    public AccountClock() { this(Clock.systemUTC()); }
    public AccountClock(Clock clock) { this.clock = clock; }
    public LocalDate today(User user) { return LocalDate.now(clock.withZone(ZoneId.of(user.getTimezone()))); }
}
