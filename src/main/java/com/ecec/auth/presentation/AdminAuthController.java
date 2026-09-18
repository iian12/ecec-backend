package com.ecec.auth.presentation;

import com.ecec.auth.application.AdminAuthService;
import com.ecec.auth.application.TokenRefreshService;
import com.ecec.auth.application.result.LoginSuccessResult;
import com.ecec.auth.infrastructure.jwt.JwtProperties;
import com.ecec.auth.infrastructure.jwt.TokenCookieProperties;
import com.ecec.auth.presentation.request.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {
    private final AdminAuthService auth;
    private final TokenRefreshService refreshTokens;
    private final JwtProperties jwt;
    private final TokenCookieProperties cookies;

    public AdminAuthController(AdminAuthService auth, TokenRefreshService refreshTokens,
                               JwtProperties jwt, TokenCookieProperties cookies) {
        this.auth = auth;
        this.refreshTokens = refreshTokens;
        this.jwt = jwt;
        this.cookies = cookies;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        return tokenResponse(auth.login(request.toCommand()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@CookieValue(name = "refresh_token", required = false) String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        return tokenResponse(refreshTokens.refreshAdmin(token));
    }

    private ResponseEntity<Void> tokenResponse(LoginSuccessResult result) {
        // 토큰은 JSON에 노출하지 않고 HttpOnly 쿠키로만 반환한다.
        // 리프레시 토큰은 관리자 인증 경로에만 전송하도록 범위를 제한한다.
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE,
                        cookie("access_token", result.accessToken(), "/api/v1", jwt.accessTokenExpiration()),
                        cookie("refresh_token", result.refreshToken(), "/api/v1/admin/auth", jwt.refreshTokenExpiration()))
                .build();
    }

    private String cookie(String name, String value, String path, Duration maxAge) {
        return ResponseCookie.from(name, value).httpOnly(true).secure(cookies.secure())
                .sameSite(cookies.sameSite()).path(path).maxAge(maxAge).build().toString();
    }
}
