package com.ecec.auth.domain.account;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public record AuthAccountId(Long value) implements Serializable {

    public AuthAccountId {
        if (value == null) throw new IllegalArgumentException("Auth Account ID must not be null");
    }

    public static AuthAccountId of(Long value) {
        return new AuthAccountId(value);
    }
}
