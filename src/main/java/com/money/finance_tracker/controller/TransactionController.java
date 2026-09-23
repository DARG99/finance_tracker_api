package com.money.finance_tracker.controller;

import com.money.finance_tracker.dto.PageResponseDto;
import com.money.finance_tracker.dto.TransactionDto;
import com.money.finance_tracker.dto.TransactionResponseDto;
import com.money.finance_tracker.dto.TransactionUpdateDto;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDto> addTransaction(
            @Valid @RequestBody TransactionDto dto,
            @AuthenticationPrincipal User user
    ) {
        TransactionResponseDto transaction =
                transactionService.addTransaction(dto, user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transaction);
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<TransactionResponseDto>> getTransactions(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(
                transactionService.getTransactions(user, page, size)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(
                transactionService.getTransactionById(id, user)
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> patchTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateDto dto,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(
                transactionService.patchTransaction(id, dto, user)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        transactionService.deleteTransaction(id, user);

        return ResponseEntity.noContent().build();
    }
}