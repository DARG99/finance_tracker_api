package com.money.finance_tracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "finance.scheduling.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableScheduling
public class SchedulingConfig {
}