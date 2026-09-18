package com.ecec.auth.application;

import com.ecec.auth.application.result.LoginSuccessResult;
import com.ecec.auth.domain.token.RefreshToken;
import com.ecec.auth.domain.token.RefreshTokenRepository;
import com.ecec.auth.infrastructure.jwt.JwtAccessTokenProvider;
import com.ecec.auth.util.RefreshTokenGenerator;
import com.ecec.auth.util.RefreshTokenHasher;
import com.ecec.global.id.IdGenerator;
import com.ecec.user.domain.AccountStatus;
import com.ecec.user.domain.Role;
import org.springframework.security.access.AccessDeniedException;
import com.ecec.user.domain.UserId;
import com.ecec.user.domain.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;

@Service
@Transactional
public class TokenRefreshService {
    private final RefreshTokenRepository repository;
    private final RefreshTokenGenerator generator;
    private final RefreshTokenHasher hasher;
    private final IdGenerator idGenerator;
    private final UserRepository users;
    private final JwtAccessTokenProvider accessTokens;
    private final Clock clock;

    public TokenRefreshService(RefreshTokenRepository repository, RefreshTokenGenerator generator,
                               RefreshTokenHasher hasher, IdGenerator idGenerator, UserRepository users,
                               JwtAccessTokenProvider accessTokens, Clock clock) {
        this.repository = repository;
        this.generator = generator;
        this.hasher = hasher;
        this.idGenerator = idGenerator;
        this.users = users;
        this.accessTokens = accessTokens;
        this.clock = clock;
    }

    public String issue(UserId userId) {
        var issued = generator.generate();
        // 원본은 클라이언트에 한 번 전달하고 DB에는 검증용 해시와 만료 시각만 저장한다.
        repository.save(new RefreshToken(idGenerator.nextId(), userId,
                hasher.hash(issued.rawToken()), issued.expiresAt()));
        return issued.rawToken();
    }

    public LoginSuccessResult refresh(String rawToken) {
        return refresh(rawToken, false);
    }

    public LoginSuccessResult refreshAdmin(String rawToken) {
        return refresh(rawToken, true);
    }

    private LoginSuccessResult refresh(String rawToken, boolean adminOnly) {
        var token = repository.findByHashForUpdate(hasher.hash(rawToken))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if (!clock.instant().isBefore(token.expiresAt())) {
            throw new BadCredentialsException("Expired refresh token");
        }
        var user = users.findById(token.userId())
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new DisabledException("Account is not active");
        }
        // 관리자 권한이 회수된 경우 기존 토큰으로 관리자 세션을 갱신할 수 없다.
        if (adminOnly && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Administrator role required");
        }
        // 기존 토큰 폐기와 새 토큰 저장을 한 트랜잭션으로 처리해 재사용을 막는다.
        repository.delete(token);
        return new LoginSuccessResult(accessTokens.createAccessToken(user.getId(), user.getRole()),
                issue(user.getId()));
    }
}
