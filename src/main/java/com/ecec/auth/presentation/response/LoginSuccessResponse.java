package com.ecec.auth.presentation.response;

public record LoginSuccessResponse(String type, String accessToken, String refreshToken) implements LoginResponse {
    public LoginSuccessResponse(String accessToken, String refreshToken) {
        this("LOGIN_SUCCESS", accessToken, refreshToken);
    }
}
