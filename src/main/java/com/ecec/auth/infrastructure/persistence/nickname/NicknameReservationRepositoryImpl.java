package com.ecec.auth.infrastructure.persistence.nickname;

import com.ecec.auth.domain.nickname.*;
import com.ecec.auth.exception.AuthRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.sql.SQLException;
import java.util.Optional;
import jakarta.persistence.EntityManager;

@Repository
@Transactional(readOnly = true)
public class NicknameReservationRepositoryImpl implements NicknameReservationRepository {
    private final NicknameReservationJpaRepository repository;
    private final EntityManager entityManager;

    public NicknameReservationRepositoryImpl(NicknameReservationJpaRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    public Optional<NicknameReservation> findByNickname(String nickname) {
        return repository.findById(nickname).map(NicknameReservationEntity::toDomain);
    }

    @Transactional
    public Optional<NicknameReservation> findByNicknameForUpdate(String nickname) {
        return repository.findForUpdate(nickname).map(NicknameReservationEntity::toDomain);
    }

    @Transactional
    public void save(NicknameReservation reservation) {
        var entity = repository.findById(reservation.nickname()).map(existing -> {
            existing.update(reservation);
            return existing;
        }).orElseThrow();
        repository.saveAndFlush(entity);
    }

    @Transactional
    public void insert(NicknameReservation reservation) {
        try {
            // merge/upsert는 다른 요청의 예약을 덮어쓸 수 있어 최초 생성에는 INSERT만 사용한다.
            entityManager.persist(new NicknameReservationEntity(reservation));
            entityManager.flush();
        } catch (RuntimeException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof SQLException sql && (sql.getErrorCode() == 1062 || "23505".equals(sql.getSQLState()))) {
                    throw new AuthRequestException("NICKNAME_RESERVED", "nickname", "다른 요청에서 먼저 예약한 닉네임입니다.", true);
                }
            }
            throw exception;
        }
    }
}
