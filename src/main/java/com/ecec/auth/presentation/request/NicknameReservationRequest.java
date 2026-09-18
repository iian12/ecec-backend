package com.ecec.auth.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameReservationRequest(
        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(max = 255, message = "닉네임은 255자 이하여야 합니다.") String nickname) {
}
