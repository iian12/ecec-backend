package com.ecec.auth.domain.verification;

import com.ecec.auth.exception.*;
import com.ecec.user.domain.UserId;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;

class EmailVerificationTest {
    private final Instant now = Instant.parse("2026-09-14T00:00:00Z");
    private EmailVerification create() {
        return EmailVerification.create(EmailVerificationId.of(1L), UserId.of(1L), "correct-hash", now.plusSeconds(300), now);
    }

    @Test
    void expiresAtExactBoundary() {
        var verification = create();
        assertThat(verification.isExpired(now.plusSeconds(299))).isFalse();
        assertThatThrownBy(() -> verification.verify("correct-hash", now.plusSeconds(300)))
                .isInstanceOf(ExpiredVerificationCodeException.class);
        assertThat(verification.isVerified()).isFalse();
    }

    @Test
    void fifthFailureLocksCodeIncludingSubsequentCorrectAttempts() {
        var verification = create();
        for (int attempt = 1; attempt < 5; attempt++) {
            assertThatThrownBy(() -> verification.verify("wrong-hash", now)).isInstanceOf(InvalidVerificationCodeException.class);
            assertThat(verification.getFailedAttempts()).isEqualTo(attempt);
        }
        assertThatThrownBy(() -> verification.verify("wrong-hash", now)).isInstanceOf(VerificationAttemptsExceededException.class);
        assertThat(verification.getFailedAttempts()).isEqualTo(5);
        assertThatThrownBy(() -> verification.verify("correct-hash", now)).isInstanceOf(VerificationAttemptsExceededException.class);
        assertThat(verification.isVerified()).isFalse();
    }

    @Test
    void successfulCodeCannotBeReused() {
        var verification = create();
        verification.verify("correct-hash", now);
        assertThat(verification.getVerifiedAt()).isEqualTo(now);
        assertThatThrownBy(() -> verification.verify("correct-hash", now)).isInstanceOf(AlreadyVerifiedException.class);
    }

    @Test
    void refusesZeroLifetime() {
        assertThatIllegalArgumentException().isThrownBy(() -> EmailVerification.create(
                EmailVerificationId.of(1L), UserId.of(1L), "hash", now, now));
    }
}
