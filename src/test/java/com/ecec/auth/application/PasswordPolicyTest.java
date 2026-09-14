package com.ecec.auth.application;

import com.ecec.auth.exception.AuthRequestException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

class PasswordPolicyTest {
    @ParameterizedTest
    @ValueSource(strings = {"Abcdefg!", "Abcdefghijklmnopqrs!", "Abcdefghijklmnopqr!1", "Abcdef1!", "Abcdefg_"})
    void acceptsRequiredCharactersAndLengthBoundaries(String password) {
        assertThatCode(() -> PasswordPolicy.validate(password)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Abcdef!", "Abcdefghijklmnopqrs!1", "abcdefg!", "ABCDEFG!", "Abcdefgh", "Abcdef !", "Abcdef가!", "Abcdefg!\n"})
    void rejectsInvalidPasswordWithoutLeakingInput(String password) {
        assertThatThrownBy(() -> PasswordPolicy.validate(password))
                .isInstanceOfSatisfying(AuthRequestException.class, error -> {
                    assertThat(error.code()).isEqualTo("INVALID_PASSWORD");
                    assertThat(error.field()).isEqualTo("password");
                    assertThat(error.conflict()).isFalse();
                    assertThat(error.getMessage()).isEqualTo(PasswordPolicy.MESSAGE);
                });
    }
}
