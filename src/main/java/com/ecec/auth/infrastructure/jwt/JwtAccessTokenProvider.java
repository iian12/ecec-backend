package com.ecec.auth.infrastructure.jwt;

import com.ecec.auth.application.security.AccessTokenClaims;
import com.ecec.user.domain.Role;
import com.ecec.user.domain.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

@Component
public class JwtAccessTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMillis;
    private final Clock clock;

    public JwtAccessTokenProvider(JwtProperties properties, Clock clock) {
        this.key = Keys.hmacShaKeyFor(properties.secretKey().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMillis = properties.accessTokenExpiration().toMillis();
        this.clock = clock;
    }

    public String createAccessToken(UserId userId, Role role) {
        Objects.requireNonNull(userId, "User ID must not be null.");
        Objects.requireNonNull(userId.value(), "User ID value must not be null.");
        Objects.requireNonNull(role, "Role must not be null.");

        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plusMillis(
                accessTokenExpirationMillis
        );

        return Jwts.builder()
                .subject(userId.value().toString())
                .claim("role", role.name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public AccessTokenClaims parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (claims.getExpiration() == null || claims.getSubject() == null) {
            throw new IllegalArgumentException("Token subject and expiration are required.");
        }
        UserId userId = UserId.of(Long.parseLong(claims.getSubject()));

        String roleClaim = claims.get("role", String.class);

        if (roleClaim == null || roleClaim.isBlank()) {
            throw new IllegalArgumentException("Role claim is missing in the token.");
        }

        Role role;

        try {
            role = Role.valueOf(roleClaim);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid role claim: " + roleClaim,
                    exception
            );
        }

        return new AccessTokenClaims(
                userId,
                role
        );
    }

    public long getExpirationSeconds() {
        return accessTokenExpirationMillis / 1_000;
    }
}
