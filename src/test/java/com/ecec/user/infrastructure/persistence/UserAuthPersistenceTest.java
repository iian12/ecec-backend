package com.ecec.user.infrastructure.persistence;

import com.ecec.auth.domain.account.*;
import com.ecec.auth.domain.verification.*;
import com.ecec.auth.exception.InvalidVerificationCodeException;
import com.ecec.user.domain.*;
import com.ecec.user.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserAuthPersistenceTest {
    @Autowired UserRepository users;
    @Autowired AuthAccountRepository accounts;
    @Autowired EmailVerificationRepository verifications;
    @Autowired EntityManager entityManager;

    private void reload() { entityManager.flush(); entityManager.clear(); }

    @Test
    void profileImageIsPreservedOnInsertAndUpdate() {
        var id = UserId.of(123L);
        users.save(User.restore(id, "image@example.com", "profile", "first.png", Role.USER, AccountStatus.ACTIVE));
        reload();
        var user = users.findById(id).orElseThrow();
        assertThat(user.getProfileImgPath()).isEqualTo("first.png");
        user.changeProfileImgUrl("second.png");
        user.block();
        users.save(user);
        reload();
        var restored = users.findById(id).orElseThrow();
        assertThat(restored.getProfileImgPath()).isEqualTo("second.png");
        assertThat(restored.getAccountStatus()).isEqualTo(AccountStatus.BLOCKED);
    }

    @Test
    void restoredAuthAccountCanBeSavedWithoutDuplicateInsert() {
        accounts.save(AuthAccount.createLocal(AuthAccountId.of(123L), UserId.of(123L), "old@example.com", "first-hash"));
        reload();
        var account = accounts.findLocalByEmail("old@example.com").orElseThrow();
        var time = Instant.parse("2026-09-14T00:00:00Z");
        account.updateEmail("new@example.com");
        account.updatePassword("new-hash");
        account.updateLastLoginAt(time);
        account.updateUpdatedAt(time);
        accounts.save(account);
        reload();
        assertThat(accounts.findLocalByEmail("old@example.com")).isEmpty();
        var restored = accounts.findLocalByEmail("new@example.com").orElseThrow();
        assertThat(restored.getId()).isEqualTo(AuthAccountId.of(123L));
        assertThat(restored.getEncodedPassword()).isEqualTo("new-hash");
        assertThat(restored.getLastLoginAt()).isEqualTo(time);
        assertThat(restored.getUpdatedAt()).isEqualTo(time);
    }

    @Test
    void verificationUpdatesPersistFailureCountAndVerifiedTime() {
        var time = Instant.parse("2026-09-14T00:00:00Z");
        var id = EmailVerificationId.of(123L);
        verifications.save(EmailVerification.create(id, UserId.of(123L), "correct", time.plusSeconds(300), time));
        reload();
        var verification = verifications.findById(id).orElseThrow();
        assertThatThrownBy(() -> verification.verify("wrong", time)).isInstanceOf(InvalidVerificationCodeException.class);
        verifications.save(verification);
        reload();
        var restored = verifications.findById(id).orElseThrow();
        assertThat(restored.getFailedAttempts()).isEqualTo(1);
        restored.verify("correct", time);
        verifications.save(restored);
        reload();
        assertThat(verifications.findById(id).orElseThrow().getVerifiedAt()).isEqualTo(time);
    }
}
