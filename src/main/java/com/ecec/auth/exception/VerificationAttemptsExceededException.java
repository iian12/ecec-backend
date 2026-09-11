package com.ecec.auth.exception;

public class VerificationAttemptsExceededException extends RuntimeException {

    public VerificationAttemptsExceededException() {
        super("Email verification attempts have been exceeded.");
    }
}
