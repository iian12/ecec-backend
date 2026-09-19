package com.ecec.user.infrastructure.persistence;

import com.ecec.global.id.AssignedIdEntity;
import com.ecec.user.domain.Role;
import com.ecec.user.domain.AccountStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class UserEntity extends AssignedIdEntity {

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    @Column
    private String profileImgPath;

    @Column
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    @Builder
    public UserEntity(Long id, String email, String nickname, String profileImgPath, Role role, AccountStatus accountStatus) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.profileImgPath = profileImgPath;
        this.role = role;
        this.accountStatus = accountStatus;
    }

    void setNickname(String nickname) {
        this.nickname = nickname;
    }

    void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    void setProfileImgPath(String profileImgPath) {
        this.profileImgPath = profileImgPath;
    }
}
