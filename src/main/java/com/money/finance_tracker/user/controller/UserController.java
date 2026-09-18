package com.money.finance_tracker.user.controller;

import com.money.finance_tracker.user.dto.FundingSourceDto;
import com.money.finance_tracker.user.dto.UserDto;
import com.money.finance_tracker.user.dto.UserSignUpDto;
import com.money.finance_tracker.user.entity.FundingSource;
import com.money.finance_tracker.user.service.UserService;
import com.money.finance_tracker.user.util.PasswordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserSignUpDto signUpDto) {
        UserDto created = userService.createUser(signUpDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/funding-sources")
    public ResponseEntity<String> addFundingSource(@Valid @RequestBody FundingSourceDto fundingSourceDto) {
        FundingSource created = userService.addFundingSource(fundingSourceDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Funding source added successfully");
    }
}
