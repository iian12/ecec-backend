package com.ecec.auth.application;

import com.ecec.auth.domain.verification.*;
import com.ecec.auth.util.EmailVerificationCodeHasher;
import com.ecec.user.domain.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.security.SecureRandom;
import java.time.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailVerificationServiceTest {
    @Test
    void savesHashOfZeroPaddedCodeWithExactlyFiveMinutesLifetime() {
        var repository = mock(EmailVerificationRepository.class);
        var random = mock(SecureRandom.class);
        when(random.nextInt(1_000_000)).thenReturn(42);
        Instant now = Instant.parse("2026-09-14T00:00:00Z");
        var hasher = new EmailVerificationCodeHasher();
        var service = new EmailVerificationService(repository, () -> 123L, random,
                Clock.fixed(now, ZoneOffset.UTC), hasher);
        var id = service.create(UserId.of(1L));
        var captor = ArgumentCaptor.forClass(EmailVerification.class);
        verify(repository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getUserId()).isEqualTo(UserId.of(1L));
        assertThat(saved.getCreatedAt()).isEqualTo(now);
        assertThat(saved.getExpiresAt()).isEqualTo(now.plusSeconds(300));
        assertThat(saved.getVerificationCodeHash()).isEqualTo(hasher.hash("000042")).isNotEqualTo("000042");
        saved.verify(hasher.hash("000042"), now.plusSeconds(299));
        assertThat(saved.isVerified()).isTrue();
    }
}
