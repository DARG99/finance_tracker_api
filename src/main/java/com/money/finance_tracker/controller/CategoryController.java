package com.money.finance_tracker.controller;

import com.money.finance_tracker.dto.CategoryDto;
import com.money.finance_tracker.dto.CategoryResponseDto;
import com.money.finance_tracker.dto.FundingSourceDto;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Void> addCategory(
            @Valid @RequestBody CategoryDto dto,
            @AuthenticationPrincipal User user
    ) {
        categoryService.addCategory(dto, user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getCategories(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(
                categoryService.getCategories(user)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        categoryService.deleteCategory(id, user);

        return ResponseEntity.noContent().build();
    }
}
