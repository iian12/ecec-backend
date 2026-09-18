package com.ecec.auth.presentation;

import com.ecec.auth.exception.AuthRequestException;
import com.ecec.auth.presentation.response.AuthErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = {AuthController.class, AdminAuthController.class})
public class AuthExceptionHandler {
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<AuthErrorResponse> adminRequired() {
        return error(HttpStatus.FORBIDDEN, "ADMIN_REQUIRED", "관리자 권한이 필요합니다.", Map.of());
    }
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<AuthErrorResponse> concurrentRequest() {
        // MariaDB에서 최초 예약의 동시 INSERT가 데드락으로 판정되는 경우에도 재시도 가능한 오류를 반환한다.
        return error(HttpStatus.CONFLICT, "CONCURRENT_REQUEST", "동시에 처리 중인 요청이 있습니다. 다시 시도해 주세요.", Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthErrorResponse> invalidFields(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new AuthErrorResponse(
                "INVALID_REQUEST", "입력값을 확인해 주세요.", fields));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AuthErrorResponse> invalidBody() {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY",
                "요청 본문이 없거나 JSON 형식이 올바르지 않습니다.", Map.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<AuthErrorResponse> missingParameter(MissingServletRequestParameterException exception) {
        return error(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", "필수 요청 파라미터가 없습니다.",
                Map.of(exception.getParameterName(), "값을 입력해 주세요."));
    }

    @ExceptionHandler(AuthRequestException.class)
    public ResponseEntity<AuthErrorResponse> invalidRequest(AuthRequestException exception) {
        return error(exception.conflict() ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST,
                exception.code(), exception.getMessage(), Map.of(exception.field(), exception.getMessage()));
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<AuthErrorResponse> invalidCredentials() {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "이메일 또는 비밀번호가 올바르지 않습니다.", Map.of());
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<AuthErrorResponse> accountUnavailable() {
        return error(HttpStatus.FORBIDDEN, "ACCOUNT_UNAVAILABLE", "로그인할 수 없는 계정입니다.", Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<AuthErrorResponse> integrityViolation(DataIntegrityViolationException exception) {
        // 사전 중복 검사 이후 동시 가입이 발생하면 DB의 UNIQUE 제약에서 최종 차단한다.
        // MariaDB의 중복 키 오류만 409로 처리하며, 다른 무결성 오류를 중복으로 오인하지 않는다.
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getErrorCode() == 1062) {
                return error(HttpStatus.CONFLICT, "SIGN_UP_CONFLICT",
                        "이미 사용 중인 가입 정보입니다. 이메일과 닉네임을 다시 확인해 주세요.", Map.of());
            }
        }
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "요청 처리 중 오류가 발생했습니다.", Map.of());
    }

    private ResponseEntity<AuthErrorResponse> error(HttpStatus status, String code, String message,
                                                     Map<String, String> fields) {
        return ResponseEntity.status(status).body(new AuthErrorResponse(code, message, fields));
    }
}
