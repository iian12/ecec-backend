package com.ecec.user.exception;

public abstract class UserException extends RuntimeException {
    protected UserException(String message) {
        super(message);
    }
}
