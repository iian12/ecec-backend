package com.ecec.auth.domain.account;

import java.util.Optional;

public interface AuthAccountRepository {

    AuthAccount save(AuthAccount authAccount);

    Optional<AuthAccount> findByEmail(String email);

    Optional<AuthAccount> findLocalByEmail(String email);

    boolean existsByEmail(String email);
}
