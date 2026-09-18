package com.ecec.auth.application;

import com.ecec.auth.application.command.LoginCommand;
import com.ecec.auth.application.result.LoginSuccessResult;
import com.ecec.auth.infrastructure.jwt.JwtAccessTokenProvider;
import com.ecec.auth.infrastructure.security.LoginUserDetails;
import com.ecec.user.domain.AccountStatus;
import com.ecec.user.domain.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtAccessTokenProvider accessTokens;
    private final TokenRefreshService refreshTokens;

    public AdminAuthService(AuthenticationManager authenticationManager, JwtAccessTokenProvider accessTokens,
                            TokenRefreshService refreshTokens) {
        this.authenticationManager = authenticationManager;
        this.accessTokens = accessTokens;
        this.refreshTokens = refreshTokens;
    }

    @Transactional
    public LoginSuccessResult login(LoginCommand command) {
        PasswordPolicy.validate(command.password());
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(command.email(), command.password()));
        if (!(authentication.getPrincipal() instanceof LoginUserDetails user)) {
            throw new IllegalStateException("Invalid authentication principal");
        }
        // 비밀번호뿐 아니라 현재 계정 상태와 관리자 권한을 확인한 뒤에만 토큰을 발급한다.
        if (user.accountStatus() != AccountStatus.ACTIVE) {
            throw new DisabledException("Account is not active");
        }
        if (user.role() != Role.ADMIN) {
            throw new AccessDeniedException("Administrator role required");
        }
        return new LoginSuccessResult(accessTokens.createAccessToken(user.userId(), user.role()),
                refreshTokens.issue(user.userId()));
    }
}
