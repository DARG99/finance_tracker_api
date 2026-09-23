package com.money.finance_tracker.controller;

import com.money.finance_tracker.dto.DashboardResponseDto;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public ResponseEntity<DashboardResponseDto> getOverview(
            @RequestParam int year,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(
                dashboardService.getOverview(user, year)
        );
    }
}