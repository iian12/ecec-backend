package com.ecec.auth.application;

import com.ecec.auth.exception.AuthRequestException;

public final class PasswordPolicy {
    // 공백을 제외한 ASCII 문자만 허용한다. 숫자는 선택이고 대문자·소문자·특수문자는 필수다.
    public static final String REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[\\p{Punct}])[\\x21-\\x7E]{8,20}$";
    public static final String MESSAGE = "비밀번호는 영문 대문자, 소문자, 특수문자를 각각 포함한 8~20자로 입력해 주세요. 공백은 사용할 수 없습니다.";

    private PasswordPolicy() {}

    public static void validate(String password) {
        if (password == null || !password.matches(REGEX)) {
            throw new AuthRequestException("INVALID_PASSWORD", "password", MESSAGE, false);
        }
    }
}
