package com.ecec.auth.domain;

import com.ecec.user.domain.UserId;
import lombok.Getter;

import java.time.Instant;

@Getter
public class AuthAccount {

    private final AuthAccountId id;
    private final UserId userId;
    private String email;
    private String encodedPassword;
    private final AuthProvider provider;
    private final String providerUserId;
    private final Instant createdAt;
    private Instant updateAt;
    private Instant lastLoginAt;

    public AuthAccount(AuthAccountId id, UserId userId, String email, String encodedPassword, AuthProvider provider, String providerUserId, Instant createdAt, Instant updateAt, Instant lastLoginAt) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
        this.lastLoginAt = lastLoginAt;
    }

    public static AuthAccount createLocal(
            AuthAccountId id,
            UserId userId,
            String email,
            String encodedPassword
    ) {
        return new AuthAccount(id, userId, email, encodedPassword, AuthProvider.LOCAL, null, Instant.now(), null, null);
    }

    public static AuthAccount createSocial(
            AuthAccountId id,
            UserId userId,
            String email,
            AuthProvider provider,
            String providerUserId
    ) {
        if (provider == AuthProvider.LOCAL) {
            throw new IllegalArgumentException("LOCAL provider is not allowed");
        }

        return new AuthAccount(id, userId, email, null, provider, providerUserId, Instant.now(), null, null);
    }

    public void updateLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public void updatePassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateUpdateAt(Instant updateAt) {
        this.updateAt = updateAt;
    }

    public boolean isLocal() {
        return provider == AuthProvider.LOCAL;
    }

    public boolean isSocial() {
        return provider != AuthProvider.LOCAL;
    }


}
