package com.ecec.user.domain;

import lombok.Getter;

@Getter
public class User {
    private final UserId id;
    private final String email;
    private String nickname;
    private String profileImgUrl;
    private final Role role;
    private AccountStatus accountStatus;

    public User(UserId id, String email, String nickname, String profileImgUrl, Role role, AccountStatus accountStatus) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.role = role;
        this.accountStatus = accountStatus;
    }

    public static User createPendingUser(UserId id, String email) {
        return new User(id, email, null, null, Role.USER, AccountStatus.PENDING);
    }

    public static User restore(UserId id, String email, String nickname, String profileImgUrl, Role role, AccountStatus accountStatus) {
        return new User(id, email, nickname, profileImgUrl, role, accountStatus);
    }

    public void initializeNickname(String nickname) {
        if (this.nickname != null) {
            throw new IllegalStateException("Nickname is already initialized.");
        }

        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname must not be blank.");
        }

        this.nickname = nickname;
        activate();
    }

    public void updateProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    public boolean isBlocked() {
        return accountStatus == AccountStatus.BLOCKED;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public void activate() {
        this.accountStatus = AccountStatus.ACTIVE;
    }

    public void block() {
        this.accountStatus = AccountStatus.BLOCKED;
    }
}
