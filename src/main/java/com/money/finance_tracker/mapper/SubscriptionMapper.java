package com.money.finance_tracker.mapper;

import com.money.finance_tracker.dto.SubscriptionResponseDto;
import com.money.finance_tracker.entity.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public SubscriptionResponseDto toResponseDto(
            Subscription subscription
    ) {
        return new SubscriptionResponseDto(
                subscription.getId(),
                subscription.getName(),
                subscription.getAmount(),
                subscription.getFrequency(),
                subscription.getNextPaymentDate(),
                subscription.isActive(),

                subscription.getFundingSource().getId(),
                subscription.getFundingSource().getName(),

                subscription.getCategory().getId(),
                subscription.getCategory().getName()
        );
    }
}