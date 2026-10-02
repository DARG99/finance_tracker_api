package com.money.finance_tracker.config;

import com.money.finance_tracker.util.JwtService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;
import jakarta.servlet.FilterChain;

import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    @ParameterizedTest
    @ValueSource(strings = {"login", "refresh", "logout", "signup"})
    void staleBearerTokenDoesNotBlockSessionEndpoints(String endpoint) throws Exception {
        JwtService jwt = mock(JwtService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt,
                mock(UserDetailsService.class), mock(HandlerExceptionResolver.class));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/" + endpoint);
        request.setServletPath("/api/auth/" + endpoint);
        request.addHeader("Authorization", "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwt);
    }
}
