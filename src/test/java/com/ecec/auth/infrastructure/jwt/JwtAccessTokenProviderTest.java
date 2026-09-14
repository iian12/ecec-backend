package com.ecec.auth.infrastructure.jwt;

import com.ecec.user.domain.*;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.assertj.core.api.Assertions.*;

class JwtAccessTokenProviderTest {
    private final Instant now = Instant.parse("2026-09-14T00:00:00Z");
    private final String secret = "test-signing-key-at-least-thirty-two-bytes-long";
    private JwtAccessTokenProvider provider(String key, Instant instant) {
        return new JwtAccessTokenProvider(new JwtProperties(key, Duration.ofMinutes(5), Duration.ofDays(7)),
                Clock.fixed(instant, ZoneOffset.UTC));
    }

    @Test
    void roundTripPreservesLargeIdAndRole() {
        var provider = provider(secret, now);
        var id = UserId.of(9_007_199_254_740_993L);
        var claims = provider.parseAccessToken(provider.createAccessToken(id, Role.ADMIN));
        assertThat(claims.userId()).isEqualTo(id);
        assertThat(claims.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void expiredTokenIsRejectedUsingInjectedClock() {
        var token = provider(secret, now).createAccessToken(UserId.of(1L), Role.USER);
        assertThatThrownBy(() -> provider(secret, now.plusSeconds(301)).parseAccessToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void wrongSigningKeyIsRejected() {
        var token = provider(secret, now).createAccessToken(UserId.of(1L), Role.USER);
        assertThatThrownBy(() -> provider("different-key-at-least-thirty-two-bytes-long", now).parseAccessToken(token))
                .isInstanceOf(JwtException.class);
    }
}
