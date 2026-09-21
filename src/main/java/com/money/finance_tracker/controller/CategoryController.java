package com.money.finance_tracker.controller;

import com.money.finance_tracker.dto.CategoryDto;
import com.money.finance_tracker.dto.FundingSourceDto;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Void> addFundingSource(
            @Valid @RequestBody CategoryDto dto,
            @AuthenticationPrincipal User user
    ) {
        categoryService.addCategory(dto, user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
