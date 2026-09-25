package com.money.finance_tracker.controller;

import com.money.finance_tracker.dto.*;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponseDto> createSubscription(
            @Valid @RequestBody SubscriptionDto dto,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        subscriptionService.createSubscription(dto, user)
                );
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionResponseDto>> getSubscriptions(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(
                subscriptionService.getSubscriptions(user)
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDto> updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionUpdateDto dto,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(
                subscriptionService.updateSubscription(id, dto, user)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<SubscriptionResponseDto> deactivateSubscription(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(
                subscriptionService.deactivateSubscription(id, user)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        subscriptionService.deleteSubscription(id, user);

        return ResponseEntity.noContent().build();
    }
}