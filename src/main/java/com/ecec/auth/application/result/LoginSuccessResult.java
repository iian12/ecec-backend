package com.ecec.auth.application.result;

public record LoginSuccessResult(String accessToken, String refreshToken) implements LoginResult {
}
