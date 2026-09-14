package com.ecec.auth.presentation;

import com.ecec.auth.domain.account.*;
import com.ecec.auth.domain.verification.*;
import com.ecec.auth.infrastructure.persistence.verification.EmailVerificationJpaRepository;
import com.ecec.user.domain.*;
import com.ecec.user.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthHttpTest {
    @Autowired WebApplicationContext context;
    @Autowired UserRepository users;
    @Autowired AuthAccountRepository accounts;
    @Autowired EmailVerificationRepository verifications;
    @Autowired EmailVerificationJpaRepository verificationJpa;
    @Autowired PasswordEncoder encoder;
    @Autowired EntityManager entityManager;
    @Autowired JsonMapper json;
    private MockMvc mvc;
    private static final String EMAIL = "person@example.com";
    private static final String PASSWORD = "Abcdefg!";

    @BeforeEach
    void setUp() { mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); }

    private String signup(String email, String nickname, String password, String confirm) {
        return json.writeValueAsString(Map.of("email", email, "nickname", nickname, "password", password, "confirmPassword", confirm));
    }

    private String login(String password) {
        return json.writeValueAsString(Map.of("email", EMAIL, "password", password));
    }

    private void account(AccountStatus status) {
        users.save(User.restore(UserId.of(700L), EMAIL, "tester", null, Role.USER, status));
        accounts.save(AuthAccount.createLocal(AuthAccountId.of(701L), UserId.of(700L), EMAIL, encoder.encode(PASSWORD)));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void signupReturnsPersistedVerificationIdAndStoresEncodedPassword() throws Exception {
        var response = mvc.perform(post("/api/v1/auth/sign-up").contentType(MediaType.APPLICATION_JSON)
                        .content(signup(EMAIL, "tester", PASSWORD, PASSWORD)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.verificationId").isString()).andReturn();
        var body = json.readValue(response.getResponse().getContentAsString(), Map.class);
        var verificationId = EmailVerificationId.of(Long.parseLong((String) body.get("verificationId")));
        entityManager.flush();
        entityManager.clear();
        var user = users.findByEmail(EMAIL).orElseThrow();
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.PENDING);
        assertThat(verifications.findById(verificationId).orElseThrow().getUserId()).isEqualTo(user.getId());
        var password = accounts.findLocalByEmail(EMAIL).orElseThrow().getEncodedPassword();
        assertThat(password).isNotEqualTo(PASSWORD);
        assertThat(encoder.matches(PASSWORD, password)).isTrue();
    }

    @Test
    void invalidSignupFieldsReturnMessagesAndDoNotCreateUser() throws Exception {
        mvc.perform(post("/api/v1/auth/sign-up").contentType(MediaType.APPLICATION_JSON)
                        .content(signup("not-email", " ", "short", "short")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.fieldErrors.email").isString())
                .andExpect(jsonPath("$.fieldErrors.nickname").isString())
                .andExpect(jsonPath("$.fieldErrors.password").isString());
        assertThat(users.findByEmail("not-email")).isEmpty();
        assertThat(verificationJpa.count()).isZero();
    }

    @Test
    void emptyObjectAndMalformedJsonReturn400() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
    }

    @Test
    void passwordMismatchReturnsFieldError() throws Exception {
        mvc.perform(post("/api/v1/auth/sign-up").contentType(MediaType.APPLICATION_JSON)
                        .content(signup(EMAIL, "tester", PASSWORD, "Different!")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("PASSWORD_MISMATCH"))
                .andExpect(jsonPath("$.fieldErrors.confirmPassword").isString());
        assertThat(users.findByEmail(EMAIL)).isEmpty();
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        account(AccountStatus.ACTIVE);
        mvc.perform(post("/api/v1/auth/sign-up").contentType(MediaType.APPLICATION_JSON)
                        .content(signup(EMAIL, "new-name", PASSWORD, PASSWORD)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("EMAIL_ALREADY_IN_USE"));
    }

    @Test
    void nicknameIsRecheckedDuringSignup() throws Exception {
        account(AccountStatus.ACTIVE);
        mvc.perform(get("/api/v1/auth/nickname-availability").param("nickname", "tester"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));
        mvc.perform(post("/api/v1/auth/sign-up").contentType(MediaType.APPLICATION_JSON)
                        .content(signup("other@example.com", "tester", PASSWORD, PASSWORD)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("NICKNAME_ALREADY_IN_USE"));
    }

    @Test
    void nicknameCheckIsPublicAndRejectsMissingOrBlankInput() throws Exception {
        mvc.perform(get("/api/v1/auth/nickname-availability").param("nickname", "new-name"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true));
        mvc.perform(get("/api/v1/auth/nickname-availability"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("MISSING_PARAMETER"));
        mvc.perform(get("/api/v1/auth/nickname-availability").param("nickname", " "))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_NICKNAME"));
    }

    @Test
    void wrongPasswordDoesNotCreateEmailVerification() throws Exception {
        account(AccountStatus.PENDING);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(login("Wrongpass!")))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        assertThat(verificationJpa.count()).isZero();
    }

    @Test
    void pendingUserGetsVerificationInsteadOfTokens() throws Exception {
        account(AccountStatus.PENDING);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(login(PASSWORD)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.type").value("EMAIL_VERIFICATION_REQUIRED"))
                .andExpect(jsonPath("$.verificationId").isString()).andExpect(jsonPath("$.accessToken").doesNotExist());
        assertThat(verificationJpa.count()).isEqualTo(1);
    }

    @Test
    void blockedUserCannotLogin() throws Exception {
        account(AccountStatus.BLOCKED);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(login(PASSWORD)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ACCOUNT_UNAVAILABLE"));
    }

    @Test
    void activeUserCanLoginAndRotateTokenButCannotReuseOldToken() throws Exception {
        account(AccountStatus.ACTIVE);
        var response = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(login(PASSWORD)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.type").value("LOGIN_SUCCESS"))
                .andExpect(jsonPath("$.accessToken").isString()).andReturn();
        String oldToken = (String) json.readValue(response.getResponse().getContentAsString(), Map.class).get("refreshToken");
        String request = json.writeValueAsString(Map.of("refreshToken", oldToken));
        // 테스트용 가짜 토큰 대신 실제 SPA 흐름대로 쿠키를 받아 같은 값을 헤더에 전달한다.
        var csrfCookie = mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk())
                .andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertThat(csrfCookie).isNotNull();
        var refreshed = mvc.perform(post("/api/v1/auth/refresh").cookie(csrfCookie).header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk()).andReturn();
        String newToken = (String) json.readValue(refreshed.getResponse().getContentAsString(), Map.class).get("refreshToken");
        assertThat(newToken).isNotEqualTo(oldToken);
        mvc.perform(post("/api/v1/auth/refresh").cookie(csrfCookie).header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshRequiresCsrfProtection() throws Exception {
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("refreshToken", "a".repeat(43)))))
                .andExpect(status().isForbidden());
    }
}
