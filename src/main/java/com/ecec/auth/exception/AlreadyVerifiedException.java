package com.ecec.auth.exception;

public class AlreadyVerifiedException extends RuntimeException {

    public AlreadyVerifiedException() {
        super("Email verification has already been completed.");
    }
}