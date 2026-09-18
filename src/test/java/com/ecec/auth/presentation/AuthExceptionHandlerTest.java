package com.ecec.auth.presentation;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AuthExceptionHandlerTest {
    @Test
    void databaseLockConflictReturnsRetryableConflictResponse() {
        var response = new AuthExceptionHandler().concurrentRequest();
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody().code()).isEqualTo("CONCURRENT_REQUEST");
    }
}
