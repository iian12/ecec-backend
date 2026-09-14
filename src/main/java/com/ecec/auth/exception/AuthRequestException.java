package com.ecec.auth.exception;

public class AuthRequestException extends RuntimeException {
    private final String code;
    private final String field;
    private final boolean conflict;

    public AuthRequestException(String code, String field, String message, boolean conflict) {
        super(message);
        this.code = code;
        this.field = field;
        this.conflict = conflict;
    }

    public String code() { return code; }
    public String field() { return field; }
    public boolean conflict() { return conflict; }
}
