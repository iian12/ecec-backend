package com.ecec.user.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class UserTest {
    @Test
    void blockedUserCannotBecomeActiveThroughNicknameInitialization() {
        var user = User.createPendingSocialUser(UserId.of(1L), "user@example.com");
        user.block();
        assertThatIllegalStateException().isThrownBy(() -> user.initializeNickname("name"));
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.BLOCKED);
        assertThat(user.getNickname()).isNull();
    }

    @Test
    void nicknameCanOnlyBeInitializedOnce() {
        var user = User.createPendingSocialUser(UserId.of(1L), "user@example.com");
        user.initializeNickname("name");
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThatIllegalStateException().isThrownBy(() -> user.initializeNickname("other"));
        assertThat(user.getNickname()).isEqualTo("name");
    }
}
