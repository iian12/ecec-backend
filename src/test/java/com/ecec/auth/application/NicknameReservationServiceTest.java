package com.ecec.auth.application;

import com.ecec.auth.domain.nickname.*;
import com.ecec.auth.exception.AuthRequestException;
import com.ecec.auth.util.RefreshTokenHasher;
import com.ecec.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import java.security.SecureRandom;
import java.time.*;
import java.util.Optional;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class NicknameReservationServiceTest {
    private final NicknameReservationRepository repository = mock(NicknameReservationRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final Instant now = Instant.parse("2026-09-14T00:00:00Z");
    private final RefreshTokenHasher hasher = new RefreshTokenHasher();
    private final NicknameReservationService service = new NicknameReservationService(repository, users,
            new SecureRandom(), Clock.fixed(now, ZoneOffset.UTC), hasher);

    @Test
    void reservationStoresOnlyHashAndExpiresInFiveMinutes() {
        var result = service.reserve("nickname");
        var captor = ArgumentCaptor.forClass(NicknameReservation.class);
        verify(repository).insert(captor.capture());
        assertThat(result.reservationToken()).matches("[A-Za-z0-9_-]{43}");
        assertThat(result.expiresAt()).isEqualTo(now.plusSeconds(300));
        assertThat(captor.getValue().tokenHash()).isEqualTo(hasher.hash(result.reservationToken()));
        assertThat(captor.getValue().consumed()).isFalse();
    }

    @Test
    void activeReservationCannotBeOverwritten() {
        when(repository.findByNicknameForUpdate("nickname")).thenReturn(Optional.of(
                new NicknameReservation("nickname", "hash", now.plusSeconds(1), false)));
        assertThatThrownBy(() -> service.reserve("nickname")).isInstanceOfSatisfying(AuthRequestException.class,
                error -> assertThat(error.code()).isEqualTo("NICKNAME_RESERVED"));
        verify(repository, never()).save(any());
        verify(repository, never()).insert(any());
    }

    @Test
    void expiredReservationCanBeReplacedAtExactBoundary() {
        when(repository.findByNicknameForUpdate("nickname")).thenReturn(Optional.of(
                new NicknameReservation("nickname", "old-hash", now, false)));
        var result = service.reserve("nickname");
        var captor = ArgumentCaptor.forClass(NicknameReservation.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().tokenHash()).isEqualTo(hasher.hash(result.reservationToken())).isNotEqualTo("old-hash");
        verify(repository, never()).insert(any());
    }

    @Test
    void expiredTokenCannotBeConsumed() {
        when(repository.findByNicknameForUpdate("nickname")).thenReturn(Optional.of(
                new NicknameReservation("nickname", hasher.hash("token"), now, false)));
        assertThatThrownBy(() -> service.consume("nickname", "token")).isInstanceOf(AuthRequestException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void validTokenConsumesReservationAndCannotBeReused() {
        var reservation = new NicknameReservation("nickname", hasher.hash("token"), now.plusSeconds(1), false);
        when(repository.findByNicknameForUpdate("nickname")).thenReturn(Optional.of(reservation), Optional.of(reservation.consume()));
        service.consume("nickname", "token");
        verify(repository).save(reservation.consume());
        assertThatThrownBy(() -> service.consume("nickname", "token")).isInstanceOf(AuthRequestException.class);
        verify(repository, times(1)).save(any());
    }
}
