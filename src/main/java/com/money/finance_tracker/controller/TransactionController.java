package com.money.finance_tracker.controller;

import com.money.finance_tracker.dto.PageResponseDto;
import com.money.finance_tracker.dto.TransactionDto;
import com.money.finance_tracker.dto.TransactionResponseDto;
import com.money.finance_tracker.dto.TransactionUpdateDto;
import com.money.finance_tracker.entity.TransactionNature;
import com.money.finance_tracker.entity.TransactionTypeEnum;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
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
    public ResponseEntity<Page<TransactionResponseDto>> getTransactions(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) TransactionTypeEnum type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long fundingSourceId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) TransactionNature transactionNature,
            @PageableDefault(
                    size = 50,
                    sort = {"transactionDate", "createdAt"},
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(
                transactionService.getTransactions(
                        user,
                        type,
                        categoryId,
                        fundingSourceId,
                        search,
                        from,
                        to,
                        transactionNature,
                        pageable
                )
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

    @GetMapping("/reimbursable-expenses")
    public ResponseEntity<?> getReimbursableExpenses(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String search,
            @PageableDefault(
                    size = 20,
                    sort = {"transactionDate", "createdAt"},
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(
                transactionService.getReimbursableExpenses(
                        user,
                        search,
                        pageable
                )
        );
    }
}
