package com.ecec.auth.application.result;

import com.ecec.auth.domain.verification.EmailVerificationId;

public record SignUpResult(EmailVerificationId verificationId) {
}
