package com.ecec.auth.infrastructure.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Set;

@ConfigurationProperties(prefix = "jwt.cookie")
public record TokenCookieProperties(boolean secure, String sameSite) {
    public TokenCookieProperties {
        if (sameSite == null || !Set.of("Strict", "Lax", "None").contains(sameSite)) {
            throw new IllegalArgumentException("Cookie SameSite must be Strict, Lax, or None");
        }
        if ("None".equals(sameSite) && !secure) {
            throw new IllegalArgumentException("SameSite=None requires Secure cookies");
        }
    }
}
