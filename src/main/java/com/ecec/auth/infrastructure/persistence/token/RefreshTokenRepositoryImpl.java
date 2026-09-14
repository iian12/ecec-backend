package com.ecec.auth.infrastructure.persistence.token;

import com.ecec.auth.domain.token.RefreshToken;
import com.ecec.auth.domain.token.RefreshTokenRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
@Transactional
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {
    private final RefreshTokenJpaRepository repository;

    public RefreshTokenRepositoryImpl(RefreshTokenJpaRepository repository) {
        this.repository = repository;
    }

    public void save(RefreshToken token) {
        repository.save(new RefreshTokenEntity(token));
    }

    public Optional<RefreshToken> findByHashForUpdate(String hash) {
        return repository.findByTokenHash(hash).map(RefreshTokenEntity::toDomain);
    }

    public void delete(RefreshToken token) {
        repository.deleteById(token.id());
    }
}
