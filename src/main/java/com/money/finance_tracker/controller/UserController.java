package com.money.finance_tracker.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.money.finance_tracker.dto.PaydaySettingsDto;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/me/payday")
    public PaydaySettingsDto getPayday(
            @AuthenticationPrincipal User user) {
        return userService.getPayday(user);
    }

    @PatchMapping("/me/payday")
    public PaydaySettingsDto setPayday(
            @Valid @RequestBody PaydaySettingsDto settings,
            @AuthenticationPrincipal User user) {
        return userService.setPayday(settings, user);
    }

    @GetMapping
    public ResponseEntity<List<User>> allUsers() {
        List <User> users = userService.allUsers();

        return ResponseEntity.ok(users);
    }
}
