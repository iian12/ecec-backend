package com.ecec.auth.exception;

public class ExpiredVerificationCodeException extends RuntimeException {

    public ExpiredVerificationCodeException() {
        super("Email verification code has expired.");
    }
}