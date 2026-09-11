package com.ecec.auth.infrastructure.persistence.account;

import com.ecec.auth.domain.account.AuthAccount;
import com.ecec.auth.domain.account.AuthAccountId;
import com.ecec.user.domain.UserId;

public class AuthAccountMapper {
    private AuthAccountMapper() {}

    public static AuthAccountEntity toEntity(AuthAccount domain) {
        return AuthAccountEntity.builder()
                .id(domain.getId().value())
                .userId(domain.getUserId().value())
                .email(domain.getEmail())
                .encodedPassword(domain.getEncodedPassword())
                .provider(domain.getProvider())
                .providerUserId(domain.getProviderUserId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .lastLoginAt(domain.getLastLoginAt())
                .build();
    }

    public static AuthAccount toDomain(AuthAccountEntity entity) {
        return AuthAccount.restore(
                AuthAccountId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                entity.getEmail(),
                entity.getEncodedPassword(),
                entity.getProvider(),
                entity.getProviderUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastLoginAt()
        );
    }
}
