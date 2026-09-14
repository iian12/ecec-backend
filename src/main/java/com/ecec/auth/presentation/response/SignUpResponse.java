package com.ecec.auth.presentation.response;

// TSID는 JavaScript의 안전한 정수 범위를 넘을 수 있으므로 문자열로 전달한다.
public record SignUpResponse(String verificationId) {
}
