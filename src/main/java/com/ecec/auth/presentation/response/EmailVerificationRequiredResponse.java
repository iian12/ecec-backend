package com.ecec.auth.presentation.response;

import com.ecec.auth.application.result.EmailVerificationRequireResult;
import com.ecec.auth.domain.verification.EmailVerificationId;

public record EmailVerificationRequiredResponse(
        String type,
        String verificationId
) implements LoginResponse {

    public EmailVerificationRequiredResponse(EmailVerificationId verificationId) {
        this(
                "EMAIL_VERIFICATION_REQUIRED",
                verificationId.value().toString()
        );
    }
}
