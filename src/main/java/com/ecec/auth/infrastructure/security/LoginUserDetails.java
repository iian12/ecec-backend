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

public record LoginUserDetails(UserId userId,
                               String email,
                               String encodedPassword,
                               Role role,
                               AccountStatus accountStatus) implements UserDetails {

    public LoginUserDetails {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }

        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException("Encoded password must not be blank");
        }

        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }
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
        return enabled;
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

    public AppPrincipal toPrincipal() {
        return new AppPrincipal(userId, role);
    }
}
