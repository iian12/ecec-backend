package com.ecec.auth.application;

import com.ecec.auth.application.result.NicknameReservationResult;
import com.ecec.auth.domain.nickname.*;
import com.ecec.auth.exception.AuthRequestException;
import com.ecec.auth.util.RefreshTokenHasher;
import com.ecec.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.*;
import java.util.Base64;

@Service
@Transactional
public class NicknameReservationService {
    private final NicknameReservationRepository repository;
    private final UserRepository users;
    private final SecureRandom random;
    private final Clock clock;
    private final RefreshTokenHasher hasher;

    public NicknameReservationService(NicknameReservationRepository repository, UserRepository users,
                                      SecureRandom random, Clock clock, RefreshTokenHasher hasher) {
        this.repository = repository;
        this.users = users;
        this.random = random;
        this.clock = clock;
        this.hasher = hasher;
    }

    public NicknameReservationResult reserve(String nickname) {
        validateNickname(nickname);
        // 잠금을 획득한 다음 상태를 확인한다. 만료된 예약도 같은 행을 재사용한다.
        var existing = repository.findByNicknameForUpdate(nickname);
        if (users.existsByNickname(nickname)) {
            throw error("NICKNAME_ALREADY_IN_USE", "이미 사용 중인 닉네임입니다.", true);
        }
        Instant now = clock.instant();
        if (existing.filter(value -> value.isActive(now)).isPresent()) {
            throw error("NICKNAME_RESERVED", "다른 요청에서 예약한 닉네임입니다. 만료 후 다시 시도해 주세요.", true);
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = now.plus(Duration.ofMinutes(5));
        var reservation = new NicknameReservation(nickname, hasher.hash(token), expiresAt, false);
        if (existing.isEmpty()) repository.insert(reservation);
        else repository.save(reservation);
        return new NicknameReservationResult(nickname, token, expiresAt);
    }

    // 회원가입 트랜잭션에 참여한다. 가입 실패 시 예약 소비도 함께 롤백된다.
    public void consume(String nickname, String token) {
        validateNickname(nickname);
        if (token == null || token.isBlank()) {
            throw error("NICKNAME_RESERVATION_REQUIRED", "닉네임을 먼저 예약한 뒤 예약 토큰을 제출해 주세요.", false);
        }
        var reservation = repository.findByNicknameForUpdate(nickname)
                .orElseThrow(() -> error("INVALID_NICKNAME_RESERVATION", "유효한 닉네임 예약이 없습니다.", true));
        if (!reservation.isActive(clock.instant()) || !reservation.tokenHash().equals(hasher.hash(token))) {
            throw error("INVALID_NICKNAME_RESERVATION", "예약이 만료되었거나 예약 토큰이 올바르지 않습니다.", true);
        }
        repository.save(reservation.consume());
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(String nickname) {
        validateNickname(nickname);
        return !users.existsByNickname(nickname)
                && repository.findByNickname(nickname).filter(value -> value.isActive(clock.instant())).isEmpty();
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank() || nickname.length() > 255) {
            throw error("INVALID_NICKNAME", "닉네임은 공백이 아닌 1~255자로 입력해 주세요.", false);
        }
    }

    private AuthRequestException error(String code, String message, boolean conflict) {
        return new AuthRequestException(code, "nickname", message, conflict);
    }
}
