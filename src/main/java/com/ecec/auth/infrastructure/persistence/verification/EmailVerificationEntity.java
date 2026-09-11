package com.ecec.auth.infrastructure.persistence.verification;

import com.ecec.global.id.AssignedIdEntity;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationEntity extends AssignedIdEntity {

    private Long userId;

    private String verificationCodeHash;
    private Instant expiresAt;
    private Instant verifiedAt;

    private int failedAttempts;
    private Instant createdAt;

    @Builder
    public EmailVerificationEntity(Long id, Long userId, String verificationCodeHash, Instant expiresAt, Instant verifiedAt, int failedAttempts, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.verificationCodeHash = verificationCodeHash;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
        this.failedAttempts = failedAttempts;
        this.createdAt = createdAt;
    }

    void updateVerificationState(
            Instant verifiedAt,
            int failedAttempts
    ) {
        this.verifiedAt = verifiedAt;
        this.failedAttempts = failedAttempts;
    }
}
