package com.ecec.auth.application.result;

import java.time.Instant;

public record NicknameReservationResult(String nickname, String reservationToken, Instant expiresAt) {
}
