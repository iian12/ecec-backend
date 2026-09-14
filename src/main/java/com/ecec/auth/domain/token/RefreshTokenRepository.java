package com.ecec.auth.domain.token;

import java.util.Optional;

public interface RefreshTokenRepository {
    void save(RefreshToken token);
    Optional<RefreshToken> findByHashForUpdate(String hash);
    void delete(RefreshToken token);
}
