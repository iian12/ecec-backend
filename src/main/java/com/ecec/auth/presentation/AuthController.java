package com.ecec.auth.presentation;

import com.ecec.auth.application.AuthService;
import com.ecec.auth.application.TokenRefreshService;
import com.ecec.auth.application.NicknameReservationService;
import com.ecec.auth.application.result.NicknameReservationResult;
import com.ecec.auth.presentation.request.NicknameReservationRequest;
import com.ecec.auth.presentation.request.TokenRefreshRequest;
import com.ecec.auth.application.result.EmailVerificationRequireResult;
import com.ecec.auth.application.result.LoginResult;
import com.ecec.auth.application.result.LoginSuccessResult;
import com.ecec.auth.domain.verification.EmailVerificationId;
import com.ecec.auth.presentation.request.LoginRequest;
import com.ecec.auth.presentation.request.SignUpRequest;
import com.ecec.auth.presentation.response.EmailVerificationRequiredResponse;
import com.ecec.auth.presentation.response.LoginResponse;
import com.ecec.auth.presentation.response.LoginSuccessResponse;
import com.ecec.auth.presentation.response.NicknameAvailabilityResponse;
import com.ecec.auth.presentation.response.SignUpResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenRefreshService tokenRefreshService;
    private final NicknameReservationService nicknameReservations;

    public AuthController(AuthService authService, TokenRefreshService tokenRefreshService, NicknameReservationService nicknameReservations) {
        this.authService = authService;
        this.tokenRefreshService = tokenRefreshService;
        this.nicknameReservations = nicknameReservations;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        var result = authService.signUp(request.toCommand());
        return ResponseEntity.ok(new SignUpResponse(result.verificationId().value().toString()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.localUserLogin(request.toCommand());

        LoginResponse response = switch (result) {
            case LoginSuccessResult(String accessToken, String refreshToken) ->
                    new LoginSuccessResponse(accessToken, refreshToken);

            case EmailVerificationRequireResult(EmailVerificationId verificationId) ->
                    new EmailVerificationRequiredResponse(
                            verificationId
                    );
        };

        return ResponseEntity.ok(response);
    }

    @GetMapping("/nickname-availability")
    public NicknameAvailabilityResponse nicknameAvailability(@RequestParam("nickname") String nickname) {
        return new NicknameAvailabilityResponse(authService.isNicknameAvailable(nickname));
    }

    @PostMapping("/refresh")
    public LoginSuccessResponse refresh(@Valid @RequestBody TokenRefreshRequest request) {
        var result = tokenRefreshService.refresh(request.refreshToken());
        return new LoginSuccessResponse(result.accessToken(), result.refreshToken());
    }

    // 검사와 예약을 한 번에 수행한다. 토큰이 포함된 응답은 캐시하지 않는다.
    @PostMapping("/nickname-reservations")
    public ResponseEntity<NicknameReservationResult> reserveNickname(@Valid @RequestBody NicknameReservationRequest request) {
        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noStore())
                .body(nicknameReservations.reserve(request.nickname()));
    }
}
