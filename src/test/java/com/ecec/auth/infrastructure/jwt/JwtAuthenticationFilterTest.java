package com.ecec.auth.infrastructure.jwt;

import com.ecec.auth.application.security.AccessTokenClaims;
import com.ecec.auth.infrastructure.security.AppPrincipal;
import com.ecec.user.domain.*;
import com.ecec.user.domain.repository.UserRepository;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private final JwtAccessTokenProvider tokens = mock(JwtAccessTokenProvider.class);
    private final UserRepository users = mock(UserRepository.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokens, users);
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final FilterChain chain = mock(FilterChain.class);
    private final UserId id = UserId.of(1L);

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    private void user(AccountStatus status) {
        when(tokens.parseAccessToken("header-token")).thenReturn(new AccessTokenClaims(id, Role.ADMIN));
        when(users.findById(id)).thenReturn(Optional.of(User.restore(id, "user@example.com", "name", null, Role.USER, status)));
    }

    @Test
    void bearerHeaderWinsOverCookiesAndCurrentRoleWinsOverTokenRole() throws Exception {
        request.addHeader("Authorization", "Bearer header-token");
        request.setCookies(new Cookie("XSRF-TOKEN", "csrf"), new Cookie("access_token", "cookie-token"));
        user(AccountStatus.ACTIVE);
        filter.doFilter(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(new AppPrincipal(id, Role.USER));
        verify(tokens, never()).parseAccessToken("cookie-token");
        verify(chain).doFilter(request, response);
    }

    @Test
    void cookieTokenIsUsedWhenHeaderIsAbsent() throws Exception {
        request.setCookies(new Cookie("access_token", "header-token"));
        user(AccountStatus.ACTIVE);
        filter.doFilter(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void malformedTokenContinuesOnceWithoutAuthentication() throws Exception {
        request.addHeader("Authorization", "Bearer bad-token");
        when(tokens.parseAccessToken("bad-token")).thenThrow(new MalformedJwtException("invalid"));
        filter.doFilter(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, times(1)).doFilter(request, response);
        verifyNoInteractions(users);
    }

    @Test
    void controllerExceptionMustNotReexecuteFilterChain() throws Exception {
        doThrow(new IllegalArgumentException("controller failure")).when(chain).doFilter(request, response);
        assertThatIllegalArgumentException().isThrownBy(() -> filter.doFilter(request, response, chain));
        verify(chain, times(1)).doFilter(request, response);
    }

    @ParameterizedTest
    @EnumSource(value = AccountStatus.class, names = {"PENDING", "BLOCKED"})
    void inactiveUserCannotAuthenticateWithPreviouslyIssuedToken(AccountStatus status) throws Exception {
        request.addHeader("Authorization", "Bearer header-token");
        user(status);
        filter.doFilter(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void deletedUserCannotAuthenticate() throws Exception {
        request.addHeader("Authorization", "Bearer header-token");
        when(tokens.parseAccessToken("header-token")).thenReturn(new AccessTokenClaims(id, Role.USER));
        filter.doFilter(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
