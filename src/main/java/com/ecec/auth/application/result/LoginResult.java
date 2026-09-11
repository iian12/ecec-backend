package com.ecec.auth.application.result;

import java.time.Instant;

public sealed interface LoginResult permits LoginSuccessResult, EmailVerificationRequireResult {
}
