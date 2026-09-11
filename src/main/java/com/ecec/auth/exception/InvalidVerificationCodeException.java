package com.ecec.auth.exception;

public class InvalidVerificationCodeException extends RuntimeException {

    public InvalidVerificationCodeException() {
        super("Email verification code is invalid.");
    }
}
