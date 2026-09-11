package com.ecec.auth.application;

import com.ecec.auth.application.command.LoginCommand;
import com.ecec.auth.application.command.SignUpCommand;
import com.ecec.auth.application.result.LoginResult;
import com.ecec.auth.application.result.LoginSuccessResult;
import com.ecec.auth.application.result.SignUpResult;
import com.ecec.auth.domain.account.AuthAccount;
import com.ecec.auth.domain.account.AuthAccountId;
import com.ecec.auth.domain.account.AuthAccountRepository;
import com.ecec.auth.domain.verification.EmailVerification;
import com.ecec.auth.domain.verification.EmailVerificationId;
import com.ecec.auth.infrastructure.jwt.JwtAccessTokenProvider;
import com.ecec.auth.infrastructure.security.LoginUserDetails;
import com.ecec.auth.util.RefreshTokenGenerator;
import com.ecec.auth.util.RefreshTokenHasher;
import com.ecec.global.id.IdGenerator;
import com.ecec.user.domain.AccountStatus;
import com.ecec.user.domain.User;
import com.ecec.user.domain.UserId;
import com.ecec.user.domain.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;
    private final IdGenerator idGenerator;
    private final JwtAccessTokenProvider jwtAccessTokenProvider;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final Clock clock;
    private final RefreshTokenHasher refreshTokenHasher;

    public AuthService(
            UserRepository userRepository,
            AuthAccountRepository authAccountRepository,
            IdGenerator idGenerator,
            JwtAccessTokenProvider jwtAccessTokenProvider,
            RefreshTokenGenerator refreshTokenGenerator,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            Clock clock,
            RefreshTokenHasher refreshTokenHasher
    ) {
        this.userRepository = userRepository;
        this.authAccountRepository = authAccountRepository;
        this.idGenerator = idGenerator;
        this.jwtAccessTokenProvider = jwtAccessTokenProvider;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.clock = clock;
        this.refreshTokenHasher = refreshTokenHasher;
    }

    @Transactional
    public SignUpResult signUp(SignUpCommand command) {
        if (!command.password().equals(command.confirmedPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.findByEmail(command.email()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use");
        }

        UserId userId = UserId.of(idGenerator.nextId());
        AuthAccountId authAccountId = AuthAccountId.of(idGenerator.nextId());
        EmailVerificationId emailVerificationId = EmailVerificationId.of(idGenerator.nextId());

        User user = User.createPendingLocalUser(
                userId, command.email(), command.nickname()
        );

        AuthAccount authAccount = AuthAccount.createLocal(authAccountId, userId, command.email(), passwordEncoder.encode(command.password()));

        userRepository.save(user);
        authAccountRepository.save(authAccount);


        return new SignUpResult(emailVerificationId);
    }

    @Transactional
    public LoginResult localUserLogin(LoginCommand command) {
        User user = userRepository.findByEmail(command.email()).orElseThrow();

        // 이메일 인증 절차로 넘어감
        if (user.getAccountStatus() == AccountStatus.PENDING) {
            EmailVerification emailVerification;
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(command.email(), command.password())
        );

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof LoginUserDetails loginUser)) {
            throw new IllegalStateException("Invalid authentication principal");
        }

        String refreshToken = refreshTokenHasher.hash(refreshTokenGenerator.generate().rawToken());
        return new LoginSuccessResult(
                jwtAccessTokenProvider.createAccessToken(loginUser.userId(), loginUser.role()),
                refreshToken
        );
    }
}
