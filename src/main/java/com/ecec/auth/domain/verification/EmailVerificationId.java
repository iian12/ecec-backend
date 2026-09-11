package com.ecec.auth.domain.verification;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public record EmailVerificationId(Long value) implements Serializable {
    public EmailVerificationId {
        if (value == null) throw new IllegalArgumentException("Email verification ID must not be null");
    }

    public static EmailVerificationId of(Long value) {
        return new EmailVerificationId(value);
    }
}
