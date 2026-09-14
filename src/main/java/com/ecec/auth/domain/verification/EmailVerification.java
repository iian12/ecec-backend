package com.ecec.auth.domain.verification;

import com.ecec.auth.exception.AlreadyVerifiedException;
import com.ecec.auth.exception.ExpiredVerificationCodeException;
import com.ecec.auth.exception.InvalidVerificationCodeException;
import com.ecec.auth.exception.VerificationAttemptsExceededException;
import com.ecec.user.domain.UserId;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public class EmailVerification {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final EmailVerificationId id;
    private final UserId userId;

    private final String verificationCodeHash;
    private final Instant expiresAt;

    private Instant verifiedAt;
    private int failedAttempts;

    private final Instant createdAt;

    private EmailVerification(
            EmailVerificationId id,
            UserId userId,
            String verificationCodeHash,
            Instant expiresAt,
            Instant verifiedAt,
            int failedAttempts,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.verificationCodeHash =
                requireNotBlank(verificationCodeHash, "verificationCodeHash");
        this.expiresAt =
                Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        this.createdAt =
                Objects.requireNonNull(createdAt, "createdAt must not be null");

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException(
                    "expiresAt must be after createdAt"
            );
        }

        if (failedAttempts < 0) {
            throw new IllegalArgumentException(
                    "failedAttempts must not be negative"
            );
        }

        this.verifiedAt = verifiedAt;
        this.failedAttempts = failedAttempts;
    }

    public static EmailVerification create(
            EmailVerificationId id,
            UserId userId,
            String verificationCodeHash,
            Instant expiresAt,
            Instant createdAt
    ) {
        return new EmailVerification(
                id,
                userId,
                verificationCodeHash,
                expiresAt,
                null,
                0,
                createdAt
        );
    }

    public static EmailVerification restore(
            EmailVerificationId id,
            UserId userId,
            String verificationCodeHash,
            Instant expiresAt,
            Instant verifiedAt,
            int failedAttempts,
            Instant createdAt
    ) {
        return new EmailVerification(
                id,
                userId,
                verificationCodeHash,
                expiresAt,
                verifiedAt,
                failedAttempts,
                createdAt
        );
    }

    public void verify(
            String inputCodeHash,
            Instant now
    ) {
        Objects.requireNonNull(now, "now must not be null");

        requireNotBlank(inputCodeHash, "inputCodeHash");

        if (isVerified()) {
            throw new AlreadyVerifiedException();
        }

        if (isExpired(now)) {
            throw new ExpiredVerificationCodeException();
        }

        if (hasExceededFailedAttempts()) {
            throw new VerificationAttemptsExceededException();
        }

        if (!verificationCodeHash.equals(inputCodeHash)) {
            // 호출 서비스는 인증 실패 예외가 발생해도 이 횟수를 저장해야 한다.
            // 트랜잭션 롤백으로 횟수가 사라지면 시도 횟수 제한을 우회할 수 있다.
            failedAttempts++;

            if (hasExceededFailedAttempts()) {
                throw new VerificationAttemptsExceededException();
            }

            throw new InvalidVerificationCodeException();
        }

        this.verifiedAt = now;
    }

    public boolean isVerified() {
        return verifiedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean hasExceededFailedAttempts() {
        return failedAttempts >= MAX_FAILED_ATTEMPTS;
    }

    private static String requireNotBlank(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return value;
    }
}
