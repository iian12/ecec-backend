package com.ecec.auth.application.result;

import com.ecec.auth.domain.verification.EmailVerificationId;

public record EmailVerificationRequireResult(EmailVerificationId verificationId, String email) implements LoginResult {
}
