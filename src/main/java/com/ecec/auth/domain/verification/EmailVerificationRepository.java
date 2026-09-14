package com.ecec.auth.domain.verification;

import java.util.Optional;

public interface EmailVerificationRepository {
    EmailVerification save(EmailVerification emailVerification);

    Optional<EmailVerification> findById(EmailVerificationId id);
}
