package com.ecec.auth.application;

import com.ecec.auth.application.result.IssuedRefreshToken;
import com.ecec.auth.domain.token.*;
import com.ecec.auth.infrastructure.jwt.JwtAccessTokenProvider;
import com.ecec.auth.util.*;
import com.ecec.user.domain.*;
import com.ecec.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.*;
import java.time.*;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenRefreshServiceTest {
    private final RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
    private final RefreshTokenGenerator generator = mock(RefreshTokenGenerator.class);
    private final RefreshTokenHasher hasher = new RefreshTokenHasher();
    private final UserRepository users = mock(UserRepository.class);
    private final JwtAccessTokenProvider accessTokens = mock(JwtAccessTokenProvider.class);
    private final Instant now = Instant.parse("2026-09-14T00:00:00Z");
    private final UserId userId = UserId.of(1L);
    private final TokenRefreshService service = new TokenRefreshService(repository, generator, hasher,
            () -> 10L, users, accessTokens, Clock.fixed(now, ZoneOffset.UTC));

    private RefreshToken existing(Instant expiresAt) {
        var token = new RefreshToken(2L, userId, hasher.hash("old-token"), expiresAt);
        when(repository.findByHashForUpdate(token.tokenHash())).thenReturn(Optional.of(token));
        return token;
    }

    @Test
    void issueReturnsRawTokenButPersistsOnlyHashAndExpiration() {
        when(generator.generate()).thenReturn(new IssuedRefreshToken("new-token", now.plusSeconds(600)));
        assertThat(service.issue(userId)).isEqualTo("new-token");
        var captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().tokenHash()).isEqualTo(hasher.hash("new-token")).isNotEqualTo("new-token");
        assertThat(captor.getValue().expiresAt()).isEqualTo(now.plusSeconds(600));
        assertThat(captor.getValue().userId()).isEqualTo(userId);
    }

    @Test
    void refreshDeletesOldTokenAndUsesCurrentUserRole() {
        var old = existing(now.plusSeconds(1));
        when(users.findById(userId)).thenReturn(Optional.of(User.restore(userId, "user@example.com", "name", null, Role.ADMIN, AccountStatus.ACTIVE)));
        when(generator.generate()).thenReturn(new IssuedRefreshToken("new-token", now.plusSeconds(600)));
        when(accessTokens.createAccessToken(userId, Role.ADMIN)).thenReturn("access-token");
        var result = service.refresh("old-token");
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("new-token");
        verify(repository).delete(old);
        verify(repository).save(any());
    }

    @Test
    void exactExpirationRejectsTokenWithoutMutation() {
        existing(now);
        assertThatThrownBy(() -> service.refresh("old-token")).isInstanceOf(BadCredentialsException.class);
        verify(repository, never()).delete(any());
        verify(repository, never()).save(any());
        verifyNoInteractions(generator, users, accessTokens);
    }

    @Test
    void unknownTokenIsRejected() {
        assertThatThrownBy(() -> service.refresh("unknown")).isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(generator, users, accessTokens);
    }

    @Test
    void deletedUserCannotRefresh() {
        existing(now.plusSeconds(1));
        assertThatThrownBy(() -> service.refresh("old-token")).isInstanceOf(BadCredentialsException.class);
        verify(repository, never()).delete(any());
        verifyNoInteractions(generator, accessTokens);
    }

    @ParameterizedTest
    @EnumSource(value = AccountStatus.class, names = {"PENDING", "BLOCKED"})
    void inactiveUserCannotRefresh(AccountStatus status) {
        existing(now.plusSeconds(1));
        when(users.findById(userId)).thenReturn(Optional.of(User.restore(userId, "user@example.com", "name", null, Role.USER, status)));
        assertThatThrownBy(() -> service.refresh("old-token")).isInstanceOf(DisabledException.class);
        verify(repository, never()).delete(any());
        verifyNoInteractions(generator, accessTokens);
    }
}
