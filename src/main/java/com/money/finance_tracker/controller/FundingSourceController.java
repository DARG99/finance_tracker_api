package com.money.finance_tracker.controller;

import com.money.finance_tracker.dto.FundingSourceDto;
import com.money.finance_tracker.service.FundingSourceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/funding-sources")
public class FundingSourceController {
    @Autowired
    private FundingSourceService fundingSourceService;


    @PostMapping
    public ResponseEntity<String> addFundingSource(@Valid @RequestBody FundingSourceDto fundingSourceDto) {
        fundingSourceService.addFundingSource(fundingSourceDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Funding source added successfully");
    }
}
