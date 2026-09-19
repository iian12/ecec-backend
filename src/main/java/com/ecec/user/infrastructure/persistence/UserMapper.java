package com.ecec.user.infrastructure.persistence;

import com.ecec.user.domain.User;
import com.ecec.user.domain.UserId;

public class UserMapper {
    private UserMapper() {}

    public static UserEntity toEntity(User domain) {
        return UserEntity.builder()
                .id(domain.getId().value())
                .email(domain.getEmail())
                .nickname(domain.getNickname())
                .profileImgPath(domain.getProfileImgPath())
                .role(domain.getRole())
                .accountStatus(domain.getAccountStatus())
                .build();
    }

    public static User toDomain(UserEntity entity) {
        return User.restore(
                UserId.of(entity.getId()),
                entity.getEmail(),
                entity.getNickname(),
                entity.getProfileImgPath(),
                entity.getRole(),
                entity.getAccountStatus()
        );
    }

    public static void updateEntity(User domain, UserEntity entity) {
        entity.setNickname(domain.getNickname());
        entity.setAccountStatus(domain.getAccountStatus());
        entity.setProfileImgPath(domain.getProfileImgPath());
    }
}
