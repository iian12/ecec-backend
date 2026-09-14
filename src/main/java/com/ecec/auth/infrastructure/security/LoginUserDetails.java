package com.ecec.auth.infrastructure.security;

import com.ecec.user.domain.Role;
import com.ecec.user.domain.AccountStatus;
import com.ecec.user.domain.User;
import com.ecec.user.domain.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record LoginUserDetails(UserId userId,
                               String email,
                               String encodedPassword,
                               Role role,
                               AccountStatus accountStatus) implements UserDetails {

    public LoginUserDetails {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(encodedPassword, "encodedPassword must not be null");
        Objects.requireNonNull(role, "role must not be null");
        Objects.requireNonNull(accountStatus, "accountStatus must not be null");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public String getPassword() {
        return encodedPassword;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // PENDING은 비밀번호 검증 후 인증 안내를 받아야 하므로 BLOCKED만 차단한다.
        return accountStatus != AccountStatus.BLOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public AppPrincipal toPrincipal() {
        return new AppPrincipal(userId, role);
    }
}
