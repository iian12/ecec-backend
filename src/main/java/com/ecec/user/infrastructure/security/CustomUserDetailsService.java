package com.ecec.user.infrastructure.security;

import com.ecec.auth.domain.account.AuthAccount;
import com.ecec.auth.domain.account.AuthAccountRepository;
import com.ecec.auth.infrastructure.security.LoginUserDetails;
import com.ecec.user.domain.User;
import com.ecec.user.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUserDetailsService implements UserDetailsService {

    private final AuthAccountRepository authAccountRepository;
    private final UserRepository userRepository;

    public CustomUserDetailsService(AuthAccountRepository authAccountRepository, UserRepository userRepository) {
        this.authAccountRepository = authAccountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        AuthAccount authAccount = authAccountRepository.findLocalByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        User user = userRepository.findById(authAccount.getUserId())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        return new LoginUserDetails(
                user.getId(),
                authAccount.getEmail(),
                authAccount.getEncodedPassword(),
                user.getRole(),
                user.getAccountStatus()
        );
    }
}
