package com.ecec.user.infrastructure.persistence;

import com.ecec.user.domain.User;
import com.ecec.user.domain.UserId;
import com.ecec.user.domain.repository.UserRepository;
import com.ecec.user.exception.UserNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryImpl(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity entity = jpaRepository.findById(user.getId().value())
                .map(existing -> {
                    UserMapper.updateEntity(user, existing);
                    return existing;
                })
                .orElseGet(() -> UserMapper.toEntity(user));

        return UserMapper.toDomain(
                jpaRepository.save(entity)
        );
    }

    @Override
    public Optional<User> findById(UserId userId) {

        if (userId == null) return Optional.empty();

        return jpaRepository.findById(userId.value())
                .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Objects.requireNonNull(email, "Email must not be null");

        return jpaRepository.findByEmail(email)
                .map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        Objects.requireNonNull(email, "Email must not be null");

        return jpaRepository.existsByEmail(email);
    }
}
