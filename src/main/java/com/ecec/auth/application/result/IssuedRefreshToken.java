package com.ecec.auth.application.result;

import java.time.Instant;

public record IssuedRefreshToken(String rawToken, Instant expiresAt) {
}
