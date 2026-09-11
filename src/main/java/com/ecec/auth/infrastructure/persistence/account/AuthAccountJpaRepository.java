package com.ecec.auth.infrastructure.persistence.account;

import com.ecec.auth.domain.account.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthAccountJpaRepository extends JpaRepository<AuthAccountEntity, Long> {

    Optional<AuthAccountEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<AuthAccountEntity> findByEmailAndProvider(String email, AuthProvider provider);
}
