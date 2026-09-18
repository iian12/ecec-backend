package com.ecec.auth.application;

import com.ecec.auth.application.result.NicknameReservationResult;
import com.ecec.auth.exception.AuthRequestException;
import com.ecec.auth.infrastructure.persistence.nickname.NicknameReservationJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.UUID;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class NicknameReservationIntegrationTest {
    @Autowired NicknameReservationService service;
    @Autowired NicknameReservationJpaRepository repository;
    @Autowired PlatformTransactionManager manager;

    @Test
    void consumptionRollsBackIfSignupTransactionFails() {
        String nickname = "rollback-" + UUID.randomUUID();
        var reservation = service.reserve(nickname);
        try {
            assertThatThrownBy(() -> new TransactionTemplate(manager).executeWithoutResult(status -> {
                service.consume(nickname, reservation.reservationToken());
                throw new IllegalStateException("simulate later signup failure");
            })).isInstanceOf(IllegalStateException.class);
            // 실패 이후에도 동일 토큰으로 가입을 재시도할 수 있다.
            assertThatCode(() -> service.consume(nickname, reservation.reservationToken())).doesNotThrowAnyException();
            assertThatThrownBy(() -> service.consume(nickname, reservation.reservationToken())).isInstanceOf(AuthRequestException.class);
        } finally {
            repository.deleteById(nickname);
        }
    }

    @Test
    void simultaneousRequestsHaveExactlyOneWinner() throws Exception {
        String nickname = "concurrent-" + UUID.randomUUID();
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Object> attempt = () -> {
                start.await(5, TimeUnit.SECONDS);
                try { return service.reserve(nickname); }
                catch (AuthRequestException error) { return error; }
            };
            var first = executor.submit(attempt);
            var second = executor.submit(attempt);
            start.countDown();
            var results = java.util.List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
            assertThat(results.stream().filter(NicknameReservationResult.class::isInstance).count()).isEqualTo(1);
            assertThat(results.stream().filter(AuthRequestException.class::isInstance).count()).isEqualTo(1);
            var winner = (NicknameReservationResult) results.stream().filter(NicknameReservationResult.class::isInstance).findFirst().orElseThrow();
            assertThatCode(() -> service.consume(nickname, winner.reservationToken())).doesNotThrowAnyException();
        } finally {
            repository.deleteById(nickname);
        }
    }
}
