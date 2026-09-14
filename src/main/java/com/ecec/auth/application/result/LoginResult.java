package com.ecec.auth.application.result;

public sealed interface LoginResult permits LoginSuccessResult, EmailVerificationRequireResult {
}
