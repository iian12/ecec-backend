package com.ecec.auth.application;

import com.ecec.auth.application.command.LoginCommand;
import com.ecec.auth.application.command.SignUpCommand;
import com.ecec.auth.application.result.EmailVerificationRequireResult;
import com.ecec.auth.application.result.LoginResult;
import com.ecec.auth.application.result.LoginSuccessResult;
import com.ecec.auth.application.result.SignUpResult;
import com.ecec.auth.domain.account.AuthAccount;
import com.ecec.auth.domain.account.AuthAccountId;
import com.ecec.auth.domain.account.AuthAccountRepository;
import com.ecec.auth.domain.verification.EmailVerificationId;
import com.ecec.auth.exception.AuthRequestException;
import com.ecec.auth.infrastructure.jwt.JwtAccessTokenProvider;
import com.ecec.auth.infrastructure.security.LoginUserDetails;
import com.ecec.global.id.IdGenerator;
import com.ecec.user.domain.AccountStatus;
import com.ecec.user.domain.User;
import com.ecec.user.domain.UserId;
import com.ecec.user.domain.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;
    private final IdGenerator idGenerator;
    private final JwtAccessTokenProvider jwtAccessTokenProvider;
    private final TokenRefreshService tokenRefreshService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationService emailVerificationService;

    public AuthService(
            UserRepository userRepository,
            AuthAccountRepository authAccountRepository,
            IdGenerator idGenerator,
            JwtAccessTokenProvider jwtAccessTokenProvider,
            TokenRefreshService tokenRefreshService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            EmailVerificationService emailVerificationService
    ) {
        this.userRepository = userRepository;
        this.authAccountRepository = authAccountRepository;
        this.idGenerator = idGenerator;
        this.jwtAccessTokenProvider = jwtAccessTokenProvider;
        this.tokenRefreshService = tokenRefreshService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional
    public SignUpResult signUp(SignUpCommand command) {
        PasswordPolicy.validate(command.password());
        if (!command.password().equals(command.confirmPassword())) {
            throw new AuthRequestException("PASSWORD_MISMATCH", "confirmPassword", "비밀번호가 일치하지 않습니다.", false);
        }

        if (userRepository.existsByEmail(command.email())) {
            throw new AuthRequestException("EMAIL_ALREADY_IN_USE", "email", "이미 사용 중인 이메일입니다.", true);
        }
        // 사전 검사 API는 예약이 아니므로 가입 트랜잭션에서도 다시 확인한다.
        if (!isNicknameAvailable(command.nickname())) {
            throw new AuthRequestException("NICKNAME_ALREADY_IN_USE", "nickname", "이미 사용 중인 닉네임입니다.", true);
        }

        UserId userId = UserId.of(idGenerator.nextId());
        AuthAccountId authAccountId = AuthAccountId.of(idGenerator.nextId());

        User user = User.createPendingLocalUser(
                userId, command.email(), command.nickname()
        );

        AuthAccount authAccount = AuthAccount.createLocal(authAccountId, userId, command.email(), passwordEncoder.encode(command.password()));

        userRepository.save(user);
        authAccountRepository.save(authAccount);
        // 사용자·인증 계정·인증 요청을 함께 커밋해 존재하지 않는 인증 ID 반환을 방지한다.
        EmailVerificationId emailVerificationId = emailVerificationService.create(userId);
        return new SignUpResult(emailVerificationId);
    }

    @Transactional
    public LoginResult localUserLogin(LoginCommand command) {
        PasswordPolicy.validate(command.password());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(command.email(), command.password())
        );

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof LoginUserDetails loginUser)) {
            throw new IllegalStateException("Invalid authentication principal");
        }

        // 비밀번호 검증에 성공한 경우에만 이메일 인증 요청을 생성한다.
        if (loginUser.accountStatus() == AccountStatus.PENDING) {
            return new EmailVerificationRequireResult(emailVerificationService.create(loginUser.userId()));
        }
        if (loginUser.accountStatus() != AccountStatus.ACTIVE) {
            throw new DisabledException("Account is not active");
        }

        String refreshToken = tokenRefreshService.issue(loginUser.userId());
        return new LoginSuccessResult(
                jwtAccessTokenProvider.createAccessToken(loginUser.userId(), loginUser.role()),
                refreshToken
        );
    }

    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        if (nickname == null || nickname.isBlank() || nickname.length() > 255) {
            throw new AuthRequestException("INVALID_NICKNAME", "nickname", "닉네임은 공백이 아닌 1~255자로 입력해 주세요.", false);
        }
        return !userRepository.existsByNickname(nickname);
    }

}
