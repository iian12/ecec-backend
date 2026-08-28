package com.ecec.auth.application;

import java.time.Instant;

public record IssuedRefreshToken(String rawToken, Instant expiresAt) {
}
