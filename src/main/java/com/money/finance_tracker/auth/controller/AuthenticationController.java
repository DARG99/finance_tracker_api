package com.money.finance_tracker.auth.controller;

import com.money.finance_tracker.auth.dto.LoginUserDto;
import com.money.finance_tracker.auth.dto.RegisterUserDto;
import com.money.finance_tracker.auth.dto.RefreshTokenDto;
import com.money.finance_tracker.auth.entity.LoginResponse;
import com.money.finance_tracker.auth.service.AuthenticationService;
import com.money.finance_tracker.auth.service.RefreshSessionService;
import com.money.finance_tracker.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
public class AuthenticationController {
    private final RefreshSessionService refreshSessionService;

    private final AuthenticationService authenticationService;

    public AuthenticationController(RefreshSessionService refreshSessionService, AuthenticationService authenticationService) {
        this.refreshSessionService = refreshSessionService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(refreshSessionService.create(authenticatedUser));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenDto dto) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(refreshSessionService.refresh(dto.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenDto dto) {
        refreshSessionService.logout(dto.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
