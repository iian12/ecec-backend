package com.ecec.user.domain.repository;

import com.ecec.user.domain.User;
import com.ecec.user.domain.UserId;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
}
