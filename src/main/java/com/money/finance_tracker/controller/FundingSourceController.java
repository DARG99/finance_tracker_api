package com.money.finance_tracker.controller;

import com.money.finance_tracker.dto.FundingSourceDto;
import com.money.finance_tracker.dto.FundingSourceResponseDto;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.service.FundingSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/funding-sources")
@RequiredArgsConstructor
public class FundingSourceController {

    private final FundingSourceService fundingSourceService;

    @PostMapping
    public ResponseEntity<FundingSourceResponseDto> addFundingSource(
            @Valid @RequestBody FundingSourceDto dto,
            @AuthenticationPrincipal User user
    ) {
        FundingSourceResponseDto fundingSource =
                fundingSourceService.addFundingSource(dto, user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fundingSource);
    }

    @GetMapping
    public ResponseEntity<List<FundingSourceResponseDto>> getFundingSources(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(
                fundingSourceService.getFundingSources(user)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFundingSource(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        fundingSourceService.deleteFundingSource(id, user);

        return ResponseEntity.noContent().build();
    }
}
