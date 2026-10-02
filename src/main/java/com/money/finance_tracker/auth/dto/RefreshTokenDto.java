package com.money.finance_tracker.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenDto(@NotBlank @Size(max = 128) String refreshToken) {
}
