package com.money.finance_tracker.auth.service;

import com.money.finance_tracker.auth.entity.LoginResponse;
import com.money.finance_tracker.auth.entity.RefreshSession;
import com.money.finance_tracker.auth.repository.RefreshSessionRepository;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.util.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@Transactional
public class RefreshSessionService {
    private final RefreshSessionRepository repository;
    private final JwtService jwtService;
    private final long refreshExpiration;
    private final SecureRandom random = new SecureRandom();

    public RefreshSessionService(RefreshSessionRepository repository, JwtService jwtService,
            @Value("${security.jwt.refresh-expiration-time:2592000000}") long refreshExpiration) {
        this.repository = repository;
        this.jwtService = jwtService;
        this.refreshExpiration = refreshExpiration;
    }

    public LoginResponse create(User user) {
        RefreshSession session = new RefreshSession();
        session.setUser(user);
        return rotate(session);
    }

    public LoginResponse refresh(String token) {
        RefreshSession session = repository.findByTokenHash(hash(token))
                .orElseThrow(this::invalidSession);
        if (!session.getExpiresAt().isAfter(Instant.now())) {
            throw invalidSession();
        }
        User user = session.getUser();
        if (!user.isEnabled() || !user.isAccountNonLocked()
                || !user.isAccountNonExpired() || !user.isCredentialsNonExpired()) {
            throw invalidSession();
        }
        // The row lock serializes refresh/logout. Replacing the hash consumes the old token.
        return rotate(session);
    }

    public void logout(String token) {
        repository.findByTokenHash(hash(token)).ifPresent(repository::delete);
    }

    private LoginResponse rotate(RefreshSession session) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setTokenHash(hash(token));
        session.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
        repository.save(session);

        LoginResponse response = new LoginResponse();
        response.setToken(jwtService.generateToken(session.getUser()));
        response.setExpiresIn(jwtService.getExpirationTime());
        response.setRefreshToken(token);
        response.setRefreshExpiresIn(refreshExpiration);
        return response;
    }

    private String hash(String token) {
        if (token == null || token.isBlank() || token.length() > 128) {
            throw invalidSession();
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private BadCredentialsException invalidSession() {
        return new BadCredentialsException("Session expired or invalid; please sign in again");
    }
}
