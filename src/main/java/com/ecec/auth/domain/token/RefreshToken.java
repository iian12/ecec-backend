package com.ecec.auth.domain.token;

import com.ecec.user.domain.UserId;
import java.time.Instant;

public record RefreshToken(Long id, UserId userId, String tokenHash, Instant expiresAt) {
}
