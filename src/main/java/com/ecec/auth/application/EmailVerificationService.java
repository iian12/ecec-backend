package com.ecec.auth.application;

import com.ecec.auth.domain.verification.EmailVerification;
import com.ecec.auth.domain.verification.EmailVerificationId;
import com.ecec.auth.domain.verification.EmailVerificationRepository;
import com.ecec.auth.util.EmailVerificationCodeHasher;
import com.ecec.global.id.IdGenerator;
import com.ecec.user.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
public class EmailVerificationService {
    private static final Duration VERIFICATION_LIFETIME = Duration.ofMinutes(5);

    private final EmailVerificationRepository repository;
    private final IdGenerator idGenerator;
    private final SecureRandom secureRandom;
    private final Clock clock;
    private final EmailVerificationCodeHasher codeHasher;

    public EmailVerificationService(EmailVerificationRepository repository, IdGenerator idGenerator,
                                    SecureRandom secureRandom, Clock clock,
                                    EmailVerificationCodeHasher codeHasher) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.secureRandom = secureRandom;
        this.clock = clock;
        this.codeHasher = codeHasher;
    }

    @Transactional
    public EmailVerificationId create(UserId userId) {
        EmailVerificationId id = EmailVerificationId.of(idGenerator.nextId());
        String code = String.format(Locale.ROOT, "%06d", secureRandom.nextInt(1_000_000));
        String codeHash = codeHasher.hash(code);
        Instant createdAt = clock.instant();
        repository.save(EmailVerification.create(
                id, userId, codeHash, createdAt.plus(VERIFICATION_LIFETIME), createdAt
        ));
        // TODO: 가입 및 미인증 계정 로그인 시 이메일 전송 로직을 이 위치에 연결한다.
        // userId로 수신 이메일을 조회하고, codeHash가 아닌 원본 code를 전송한다.
        // 인증 코드는 createdAt으로부터 5분간 유효하다.
        // 실제 발송은 트랜잭션 커밋 후 실행하도록 이벤트 또는 아웃박스로 연결한다.
        return id;
    }
}
