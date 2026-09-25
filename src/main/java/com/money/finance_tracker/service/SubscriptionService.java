package com.money.finance_tracker.service;

import com.money.finance_tracker.dto.*;
import com.money.finance_tracker.entity.*;
import com.money.finance_tracker.mapper.SubscriptionMapper;
import com.money.finance_tracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final FundingSourceRepository fundingSourceRepository;
    private final CategoryRepository categoryRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;

    @Transactional
    public SubscriptionResponseDto createSubscription(
            SubscriptionDto dto,
            User user
    ) {
        FundingSource fundingSource =
                findFundingSource(dto.getFundingSourceId(), user);

        Category category =
                findCategory(dto.getCategoryId(), user);

        Subscription subscription = new Subscription();

        subscription.setUser(user);
        subscription.setName(dto.getName());
        subscription.setAmount(dto.getAmount());

        subscription.setFundingSource(fundingSource);
        subscription.setCategory(category);

        subscription.setFrequency(dto.getFrequency());
        subscription.setNextPaymentDate(dto.getNextPaymentDate());

        subscription.setBillingDay(
                (short) dto.getNextPaymentDate().getDayOfMonth()
        );

        subscription.setActive(true);

        return subscriptionMapper.toResponseDto(
                subscriptionRepository.save(subscription)
        );
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponseDto> getSubscriptions(User user) {
        return subscriptionRepository
                .findAllByUserIdOrderByNextPaymentDateAsc(user.getId())
                .stream()
                .map(subscriptionMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public SubscriptionResponseDto updateSubscription(
            Long subscriptionId,
            SubscriptionUpdateDto dto,
            User user
    ) {
        Subscription subscription =
                findSubscription(subscriptionId, user);

        if (dto.getName() != null) {
            if (dto.getName().isBlank()) {
                throw new IllegalArgumentException(
                        "Subscription name cannot be blank"
                );
            }

            subscription.setName(dto.getName());
        }

        if (dto.getAmount() != null) {
            subscription.setAmount(dto.getAmount());
        }

        if (dto.getFundingSourceId() != null) {
            subscription.setFundingSource(
                    findFundingSource(dto.getFundingSourceId(), user)
            );
        }

        if (dto.getCategoryId() != null) {
            subscription.setCategory(
                    findCategory(dto.getCategoryId(), user)
            );
        }

        if (dto.getFrequency() != null) {
            subscription.setFrequency(dto.getFrequency());
        }

        if (dto.getNextPaymentDate() != null) {
            subscription.setNextPaymentDate(
                    dto.getNextPaymentDate()
            );

            subscription.setBillingDay(
                    (short) dto.getNextPaymentDate().getDayOfMonth()
            );
        }

        if (dto.getActive() != null) {
            subscription.setActive(dto.getActive());
        }

        return subscriptionMapper.toResponseDto(subscription);
    }

    @Transactional
    public SubscriptionResponseDto deactivateSubscription(
            Long subscriptionId,
            User user
    ) {
        Subscription subscription = findSubscription(subscriptionId, user);

        subscription.setActive(false);

        return subscriptionMapper.toResponseDto(subscription);
    }
    private Subscription findSubscription(
            Long subscriptionId,
            User user
    ) {
        return subscriptionRepository
                .findByIdAndUserId(subscriptionId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subscription not found"
                ));
    }

    private FundingSource findFundingSource(
            Long fundingSourceId,
            User user
    ) {
        return fundingSourceRepository
                .findByIdAndUserId(fundingSourceId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Funding source not found"
                ));
    }

    private Category findCategory(
            Long categoryId,
            User user
    ) {
        return categoryRepository
                .findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found"
                ));
    }

    @Transactional
    public void deleteSubscription(Long subscriptionId, User user) {
        Subscription subscription = subscriptionRepository
                .findByIdAndUserId(subscriptionId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Subscription not found"
                ));

        boolean hasPaymentHistory =
                subscriptionPaymentRepository.existsBySubscriptionId(
                        subscriptionId
                );

        if (hasPaymentHistory) {
            // Keep it for historical context, but stop future payments.
            subscription.setActive(false);
            return;
        }

        // No generated transaction/payment history exists.
        subscriptionRepository.delete(subscription);
    }
}