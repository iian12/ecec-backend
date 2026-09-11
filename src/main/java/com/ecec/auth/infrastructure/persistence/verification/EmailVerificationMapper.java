package com.ecec.auth.infrastructure.persistence.verification;

import com.ecec.auth.domain.verification.EmailVerification;
import com.ecec.auth.domain.verification.EmailVerificationId;
import com.ecec.user.domain.UserId;

public final class EmailVerificationMapper {
    private EmailVerificationMapper() {
    }

    public static EmailVerificationEntity toEntity(EmailVerification domain) {
        return EmailVerificationEntity.builder()
                .id(domain.getId().value())
                .userId(domain.getUserId().value())
                .verificationCodeHash(domain.getVerificationCodeHash())
                .expiresAt(domain.getExpiresAt())
                .verifiedAt(domain.getVerifiedAt())
                .failedAttempts(domain.getFailedAttempts())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public static EmailVerification toDomain(EmailVerificationEntity entity) {
        return EmailVerification.restore(
                EmailVerificationId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                entity.getVerificationCodeHash(),
                entity.getExpiresAt(),
                entity.getVerifiedAt(),
                entity.getFailedAttempts(),
                entity.getCreatedAt()
        );
    }

    public static void updateEntity(
            EmailVerification domain,
            EmailVerificationEntity entity
    ) {
        entity.updateVerificationState(
                domain.getVerifiedAt(),
                domain.getFailedAttempts()
        );
    }
}
