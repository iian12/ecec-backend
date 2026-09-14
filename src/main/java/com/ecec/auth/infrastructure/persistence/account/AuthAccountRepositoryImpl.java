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

        // 복원한 도메인을 새 엔티티로 persist하면 중복 INSERT가 발생하므로 기존 엔티티를 갱신한다.
        AuthAccountEntity entity = jpaRepository.findById(authAccount.getId().value())
                .map(existing -> {
                    existing.setEmail(authAccount.getEmail());
                    existing.setEncodedPassword(authAccount.getEncodedPassword());
                    existing.setLastLoginAt(authAccount.getLastLoginAt());
                    existing.setUpdateAt(authAccount.getUpdatedAt());
                    return existing;
                })
                .orElseGet(() -> AuthAccountMapper.toEntity(authAccount));
        AuthAccountEntity savedEntity = jpaRepository.save(entity);

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
