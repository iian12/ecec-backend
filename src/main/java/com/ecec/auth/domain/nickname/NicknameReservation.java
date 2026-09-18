package com.ecec.auth.domain.nickname;

import java.time.Instant;

public record NicknameReservation(String nickname, String tokenHash, Instant expiresAt, boolean consumed) {
    public boolean isActive(Instant now) {
        return !consumed && now.isBefore(expiresAt);
    }

    public NicknameReservation consume() {
        return new NicknameReservation(nickname, tokenHash, expiresAt, true);
    }
}
