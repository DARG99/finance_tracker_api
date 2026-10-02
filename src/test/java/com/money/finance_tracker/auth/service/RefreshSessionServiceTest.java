package com.money.finance_tracker.auth.service;

import com.money.finance_tracker.auth.entity.RefreshSession;
import com.money.finance_tracker.auth.repository.RefreshSessionRepository;
import com.money.finance_tracker.entity.User;
import com.money.finance_tracker.util.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RefreshSessionServiceTest {
    private static final long SESSION_DURATION = 2_592_000_000L;
    private RefreshSessionService service;
    private RefreshSession stored;
    private User user;

    @BeforeEach
    void setUp() {
        RefreshSessionRepository repository = mock(RefreshSessionRepository.class);
        JwtService jwt = mock(JwtService.class);
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        when(jwt.generateToken(user)).thenReturn("access-token");
        when(jwt.getExpirationTime()).thenReturn(900_000L);
        when(repository.save(any())).thenAnswer(invocation -> {
            stored = invocation.getArgument(0);
            return stored;
        });
        when(repository.findByTokenHash(anyString())).thenAnswer(invocation ->
                stored != null && stored.getTokenHash().equals(invocation.getArgument(0))
                        ? Optional.of(stored) : Optional.empty());
        doAnswer(invocation -> { stored = null; return null; })
                .when(repository).delete(any());
        service = new RefreshSessionService(repository, jwt, SESSION_DURATION);
    }

    @Test
    void createsSessionStoringOnlyTokenHash() {
        var response = service.create(user);
        assertEquals("access-token", response.getToken());
        assertEquals(900_000L, response.getExpiresIn());
        assertEquals(SESSION_DURATION, response.getRefreshExpiresIn());
        assertEquals(43, response.getRefreshToken().length());
        assertEquals(64, stored.getTokenHash().length());
        assertNotEquals(response.getRefreshToken(), stored.getTokenHash());
        assertSame(user, stored.getUser());
    }

    @Test
    void refreshRotatesTokenAndExtendsInactivityDeadline() {
        var login = service.create(user);
        stored.setExpiresAt(Instant.now().plusSeconds(60));
        Instant before = Instant.now();
        var refreshed = service.refresh(login.getRefreshToken());
        assertNotEquals(login.getRefreshToken(), refreshed.getRefreshToken());
        assertFalse(stored.getExpiresAt().isBefore(before.plusMillis(SESSION_DURATION)));
        assertThrows(BadCredentialsException.class,
                () -> service.refresh(login.getRefreshToken()));
        assertNotNull(service.refresh(refreshed.getRefreshToken()).getToken());
    }

    @Test
    void expiredSessionCannotBeRenewed() {
        var login = service.create(user);
        stored.setExpiresAt(Instant.now().minusSeconds(1));
        assertThrows(BadCredentialsException.class,
                () -> service.refresh(login.getRefreshToken()));
    }

    @Test
    void rejectsUnknownAndMissingTokens() {
        service.create(user);
        assertThrows(BadCredentialsException.class, () -> service.refresh("unknown"));
        assertThrows(BadCredentialsException.class, () -> service.refresh(null));
        assertThrows(BadCredentialsException.class, () -> service.refresh(""));
    }

    @Test
    void logoutRevokesRefreshAndIsIdempotent() {
        var login = service.create(user);
        service.logout(login.getRefreshToken());
        assertThrows(BadCredentialsException.class,
                () -> service.refresh(login.getRefreshToken()));
        assertDoesNotThrow(() -> service.logout(login.getRefreshToken()));
    }
}
