package com.ecec.auth.infrastructure.persistence.nickname;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface NicknameReservationJpaRepository extends JpaRepository<NicknameReservationEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from NicknameReservationEntity r where r.nickname = :nickname")
    Optional<NicknameReservationEntity> findForUpdate(@Param("nickname") String nickname);
}
