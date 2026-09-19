package com.ecec.user.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public class User {
    private final UserId id;
    private final String email;

    private String nickname;
    private String profileImgPath;

    private final Role role;
    private AccountStatus accountStatus;

    private User(
            UserId id,
            String email,
            String nickname,
            String profileImgPath,
            Role role,
            AccountStatus accountStatus
    ) {
        this.id = Objects.requireNonNull(id);
        this.email = requireNotBlank(email);
        this.nickname = nickname;
        this.profileImgPath = profileImgPath;
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
        // 차단된 계정이 닉네임 초기화를 통해 다시 활성화되는 것을 막는다.
        if (accountStatus != AccountStatus.PENDING) {
            throw new IllegalStateException("Only pending users can initialize a nickname.");
        }
        if (this.nickname != null) {
            throw new IllegalStateException("Nickname is already initialized.");
        }

        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname must not be blank.");
        }

        this.nickname = nickname;
        // 소셜 인증을 마친 신규 계정의 닉네임 초기화 단계에서 호출한다.
        this.accountStatus = AccountStatus.ACTIVE;
    }

    public void changeProfileImgUrl(String profileImgUrl) {
        this.profileImgPath = profileImgUrl;
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
