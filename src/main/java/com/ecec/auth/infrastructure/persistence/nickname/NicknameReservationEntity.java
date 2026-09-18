package com.ecec.auth.infrastructure.persistence.nickname;

import com.ecec.auth.domain.nickname.NicknameReservation;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "nickname_reservations")
public class NicknameReservationEntity {
    // users.nickname과 동일한 DB collation으로 중복을 판정한다.
    @Id
    @Column(nullable = false, length = 255)
    private String nickname;
    @Column(nullable = false, length = 43)
    private String tokenHash;
    @Column(nullable = false)
    private Instant expiresAt;
    @Column(nullable = false)
    private boolean consumed;

    protected NicknameReservationEntity() {}

    public NicknameReservationEntity(NicknameReservation reservation) {
        this.nickname = reservation.nickname();
        update(reservation);
    }

    void update(NicknameReservation reservation) {
        this.tokenHash = reservation.tokenHash();
        this.expiresAt = reservation.expiresAt();
        this.consumed = reservation.consumed();
    }

    NicknameReservation toDomain() {
        return new NicknameReservation(nickname, tokenHash, expiresAt, consumed);
    }
}
