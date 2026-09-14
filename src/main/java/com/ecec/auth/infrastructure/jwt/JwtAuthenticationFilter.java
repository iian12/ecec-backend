package com.ecec.auth.infrastructure.jwt;

import com.ecec.auth.application.security.AccessTokenClaims;
import com.ecec.auth.infrastructure.security.AppPrincipal;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import com.ecec.user.domain.AccountStatus;
import com.ecec.user.domain.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAccessTokenProvider jwtAccessTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtAccessTokenProvider jwtAccessTokenProvider, UserRepository userRepository) {
        this.jwtAccessTokenProvider = jwtAccessTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("/api/v1/sdk");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String accessToken = extractAccessToken(request);

            if (accessToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                AccessTokenClaims claims = jwtAccessTokenProvider.parseAccessToken(accessToken);
                // 발급 후 차단되거나 권한이 변경된 계정에도 현재 DB 상태를 적용한다.
                userRepository.findById(claims.userId())
                        .filter(user -> user.getAccountStatus() == AccountStatus.ACTIVE)
                        .ifPresent(user -> {
                            AppPrincipal principal = new AppPrincipal(user.getId(), user.getRole());
                            var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });
            }
        } catch (JwtException | IllegalArgumentException e) {
            // 잘못되거나 만료된 Access Token은 인증 객체를 생성하지 않음.
            SecurityContextHolder.clearContext();
        }
        // 하위 컨트롤러의 예외를 토큰 오류로 잡으면 요청이 재실행되므로 체인은 정확히 한 번 호출한다.
        filterChain.doFilter(request, response);
    }

    private String extractAccessToken(HttpServletRequest request) {
        // CSRF 등 다른 쿠키의 존재 여부와 무관하게 명시적인 Bearer 헤더를 우선한다.
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authorization.substring(7).strip();
        }
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
