package com.money.finance_tracker.controller;

import com.money.finance_tracker.service.AdminTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminTransactionController {

    private final AdminTransactionService adminTransactionService;

    @DeleteMapping("/users/{userId}/transactions")
    public ResponseEntity<Map<String, Integer>> deleteAllTransactionsForUser(
            @PathVariable Long userId
    ) {
        int deletedCount =
                adminTransactionService
                        .deleteAllTransactionsForUser(userId);

        return ResponseEntity.ok(
                Map.of("deletedTransactions", deletedCount)
        );
    }
}