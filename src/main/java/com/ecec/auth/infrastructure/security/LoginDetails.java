package com.ecec.auth.infrastructure.security;

import com.ecec.user.domain.Role;
import com.ecec.user.domain.AccountStatus;
import com.ecec.user.domain.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record LoginDetails(UserId userId,
                           String email,
                           String encodedPassword,
                           Role role,
                           AccountStatus accountStatus) implements UserDetails {

    public LoginDetails {
        if (userId == null) throw new IllegalArgumentException("User ID must not be null");
        if (email == null) throw new IllegalArgumentException("Email must not be null");
        if (encodedPassword == null) throw new IllegalArgumentException("Encoded password must not be null");
        if (role == null) throw new IllegalArgumentException("Role must not be null");
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return accountStatus != AccountStatus.BLOCKED;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public @Nullable String getPassword() {
        return encodedPassword;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
