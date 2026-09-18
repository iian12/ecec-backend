package com.ecec.auth.domain.nickname;

import java.util.Optional;

public interface NicknameReservationRepository {
    Optional<NicknameReservation> findByNickname(String nickname);
    Optional<NicknameReservation> findByNicknameForUpdate(String nickname);
    void save(NicknameReservation reservation);
    void insert(NicknameReservation reservation);
}
