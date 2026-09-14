package com.ecec.auth.presentation.response;

public sealed interface LoginResponse permits LoginSuccessResponse, EmailVerificationRequiredResponse {
}
