package com.ecec.user.exception;

public class UserNotFoundException extends UserException {
    public UserNotFoundException() {
        super("User not found");
    }
}
