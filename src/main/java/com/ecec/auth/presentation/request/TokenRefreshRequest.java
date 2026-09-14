package com.ecec.auth.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TokenRefreshRequest(
        @NotBlank(message = "리프레시 토큰을 입력해 주세요.")
        @Pattern(regexp = "[A-Za-z0-9_-]{43}", message = "리프레시 토큰 형식이 올바르지 않습니다.")
        String refreshToken) {
}
