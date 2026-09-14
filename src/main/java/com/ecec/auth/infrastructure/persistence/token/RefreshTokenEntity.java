package com.ecec.auth.infrastructure.persistence.token;

import com.ecec.auth.domain.token.RefreshToken;
import com.ecec.global.id.AssignedIdEntity;
import com.ecec.user.domain.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity extends AssignedIdEntity {
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false, unique = true, length = 43)
    private String tokenHash;
    @Column(nullable = false)
    private Instant expiresAt;

    protected RefreshTokenEntity() {}

    public RefreshTokenEntity(RefreshToken token) {
        this.id = token.id();
        this.userId = token.userId().value();
        this.tokenHash = token.tokenHash();
        this.expiresAt = token.expiresAt();
    }

    public RefreshToken toDomain() {
        return new RefreshToken(id, UserId.of(userId), tokenHash, expiresAt);
    }
}
