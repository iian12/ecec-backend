package com.ecec.user.domain;

import com.ecec.user.infrastructure.persistence.UserEntity;
import lombok.Getter;

import java.util.Objects;

@Getter
public class User {
    private final UserId id;
    private final String email;

    private String nickname;
    private String profileImgUrl;

    private final Role role;
    private AccountStatus accountStatus;

    private User(
            UserId id,
            String email,
            String nickname,
            String profileImgUrl,
            Role role,
            AccountStatus accountStatus
    ) {
        this.id = Objects.requireNonNull(id);
        this.email = requireNotBlank(email);
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.role = Objects.requireNonNull(role);
        this.accountStatus = Objects.requireNonNull(accountStatus);
    }

    public static User createPendingLocalUser(
            UserId id,
            String email,
            String nickname
    ) {
        return new User(
                id,
                email,
                nickname,
                null,
                Role.USER,
                AccountStatus.PENDING
        );
    }

    public static User createPendingSocialUser(
            UserId id,
            String email
    ) {
        return new User(
                id,
                email,
                null,
                null,
                Role.USER,
                AccountStatus.PENDING
        );
    }

    public static User restore(
            UserId id,
            String email,
            String nickname,
            String profileImgUrl,
            Role role,
            AccountStatus accountStatus
    ) {
        return new User(
                id,
                email,
                nickname,
                profileImgUrl,
                role,
                accountStatus
        );
    }

    public void initializeNickname(String nickname) {
        if (this.nickname != null) {
            throw new IllegalStateException("Nickname is already initialized.");
        }

        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname must not be blank.");
        }

        this.nickname = nickname;
        this.accountStatus = AccountStatus.ACTIVE;
    }

    public void changeProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    public void block() {
        this.accountStatus = AccountStatus.BLOCKED;
    }

    public boolean isPending() {
        return accountStatus == AccountStatus.PENDING;
    }

    public boolean isBlocked() {
        return accountStatus == AccountStatus.BLOCKED;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    private static String requireNotBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Value must not be blank."
            );
        }

        return value;
    }
}
