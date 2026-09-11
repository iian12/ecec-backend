package com.ecec.auth.application.security;

import com.ecec.user.domain.Role;
import com.ecec.user.domain.UserId;

public record AccessTokenClaims(UserId userId, Role role) {
}
