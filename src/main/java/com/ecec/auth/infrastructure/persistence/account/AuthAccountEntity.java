package com.ecec.auth.infrastructure.persistence.account;

import com.ecec.auth.domain.account.AuthProvider;
import com.ecec.global.id.AssignedIdEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "auth_accounts")
public class AuthAccountEntity extends AssignedIdEntity {

    @Column
    private Long userId;

    @Column
    private String email;

    @Column
    private String encodedPassword;

    @Column
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column
    private String providerUserId;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @Column
    private Instant lastLoginAt;

    @Builder
    public AuthAccountEntity(Long id, Long userId, String email, String encodedPassword, AuthProvider provider, String providerUserId, Instant createdAt, Instant updatedAt, Instant lastLoginAt) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    void setEmail(String email) {
        this.email = email;
    }

    void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    void setUpdateAt(Instant updateAt) {
        this.updatedAt = updateAt;
    }
}
