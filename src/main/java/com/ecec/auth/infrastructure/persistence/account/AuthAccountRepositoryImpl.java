package com.ecec.auth.infrastructure.persistence.account;

import com.ecec.auth.domain.account.AuthAccount;
import com.ecec.auth.domain.account.AuthProvider;
import com.ecec.auth.domain.account.AuthAccountRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Repository
public class AuthAccountRepositoryImpl implements AuthAccountRepository {

    private final AuthAccountJpaRepository jpaRepository;

    public AuthAccountRepositoryImpl(AuthAccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public AuthAccount save(AuthAccount authAccount) {
        Objects.requireNonNull(authAccount, "AuthAccount must not be null");

        AuthAccountEntity savedEntity = jpaRepository.save(AuthAccountMapper.toEntity(authAccount));

        return AuthAccountMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<AuthAccount> findByEmail(String email) {
        Objects.requireNonNull(email, "Email must not be null");

        return jpaRepository.findByEmail(email)
                .map(AuthAccountMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthAccount> findLocalByEmail(String email) {
        return jpaRepository.findByEmailAndProvider(email, AuthProvider.LOCAL)
                .map(AuthAccountMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
