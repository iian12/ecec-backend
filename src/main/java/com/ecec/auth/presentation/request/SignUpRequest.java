package com.ecec.auth.presentation.request;

import com.ecec.auth.application.command.SignUpCommand;
import com.ecec.auth.application.PasswordPolicy;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(max = 255, message = "닉네임은 255자 이하여야 합니다.") String nickname,
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "올바른 이메일 형식으로 입력해 주세요.")
        @Size(max = 255, message = "이메일은 255자 이하여야 합니다.") String email,
        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE) String password,
        @NotBlank(message = "비밀번호 확인을 입력해 주세요.")
        @Size(max = 20, message = "비밀번호 확인은 20자 이하여야 합니다.") String confirmPassword,
        @Pattern(regexp = "[A-Za-z0-9_-]{43}", message = "닉네임 예약 토큰 형식이 올바르지 않습니다.")
        String nicknameReservationToken) {
    public SignUpCommand toCommand() {
        return new SignUpCommand(email, password, confirmPassword, nickname, nicknameReservationToken);
    }
}
