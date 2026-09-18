package com.ecec.auth.presentation;

import com.ecec.auth.application.TokenRefreshService;
import com.ecec.auth.domain.account.*;
import com.ecec.auth.infrastructure.persistence.token.RefreshTokenJpaRepository;
import com.ecec.user.domain.*;
import com.ecec.user.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
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
class AdminAuthHttpTest {
    @Autowired WebApplicationContext context;
    @Autowired UserRepository users;
    @Autowired AuthAccountRepository accounts;
    @Autowired PasswordEncoder passwords;
    @Autowired RefreshTokenJpaRepository tokenRepository;
    @Autowired TokenRefreshService tokens;
    @Autowired EntityManager entityManager;
    @Autowired JsonMapper json;
    private MockMvc mvc;
    private Cookie csrf;
    private final UserId userId = UserId.of(900L);

    @BeforeEach
    void setup() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        csrf = mvc.perform(get("/api/v1/auth/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertThat(csrf).isNotNull();
    }

    private void account(Role role, AccountStatus status) {
        users.save(User.restore(userId, "admin@example.com", "admin-test", null, role, status));
        accounts.save(AuthAccount.createLocal(AuthAccountId.of(901L), userId, "admin@example.com", passwords.encode("Abcdefg!")));
        entityManager.flush();
        entityManager.clear();
    }

    private ResultActions login(String password) throws Exception {
        return mvc.perform(post("/api/v1/admin/auth/login").cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", "admin@example.com", "password", password))));
    }

    @Test
    void adminReceivesHttpOnlyCookiesAndNoTokenBody() throws Exception {
        account(Role.ADMIN, AccountStatus.ACTIVE);
        var response = login("Abcdefg!").andExpect(status().isOk())
                .andExpect(content().string("")).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(cookie().httpOnly("access_token", true)).andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().path("access_token", "/api/v1"))
                .andExpect(cookie().path("refresh_token", "/api/v1/admin/auth"))
                .andExpect(cookie().maxAge("access_token", 900))
                .andExpect(cookie().maxAge("refresh_token", 604800)).andReturn().getResponse();
        assertThat(response.getHeaders("Set-Cookie")).hasSize(2).allSatisfy(header -> assertThat(header).contains("SameSite=Lax"));
        assertThat(tokenRepository.count()).isEqualTo(1);
    }

    @Test
    void ordinaryUserIsDeniedWithoutIssuingTokens() throws Exception {
        account(Role.USER, AccountStatus.ACTIVE);
        login("Abcdefg!").andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ADMIN_REQUIRED"))
                .andExpect(cookie().doesNotExist("access_token")).andExpect(cookie().doesNotExist("refresh_token"));
        assertThat(tokenRepository.count()).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = AccountStatus.class, names = {"PENDING", "BLOCKED"})
    void inactiveAdminIsDenied(AccountStatus status) throws Exception {
        account(Role.ADMIN, status);
        login("Abcdefg!").andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ACCOUNT_UNAVAILABLE"));
        assertThat(tokenRepository.count()).isZero();
    }

    @Test
    void wrongPasswordIsDenied() throws Exception {
        account(Role.ADMIN, AccountStatus.ACTIVE);
        login("Wrongpass!").andExpect(status().isUnauthorized()).andExpect(cookie().doesNotExist("access_token"));
        assertThat(tokenRepository.count()).isZero();
    }

    @Test
    void loginRequiresCsrfEvenThoughAccessTokenIsNotRequired() throws Exception {
        mvc.perform(post("/api/v1/admin/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", "admin@example.com", "password", "Abcdefg!"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void cookieRefreshRotatesTokensAndRejectsOldCookie() throws Exception {
        account(Role.ADMIN, AccountStatus.ACTIVE);
        var refreshCookie = login("Abcdefg!").andExpect(status().isOk()).andReturn().getResponse().getCookie("refresh_token");
        var response = mvc.perform(post("/api/v1/admin/auth/refresh").cookie(csrf, refreshCookie)
                        .header("X-XSRF-TOKEN", csrf.getValue()))
                .andExpect(status().isOk()).andExpect(content().string("")).andReturn().getResponse();
        assertThat(response.getCookie("refresh_token").getValue()).isNotEqualTo(refreshCookie.getValue());
        mvc.perform(post("/api/v1/admin/auth/refresh").cookie(csrf, refreshCookie).header("X-XSRF-TOKEN", csrf.getValue()))
                .andExpect(status().isUnauthorized()).andExpect(cookie().doesNotExist("access_token"));
    }

    @Test
    void missingRefreshCookieReturns401() throws Exception {
        mvc.perform(post("/api/v1/admin/auth/refresh").cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userRefreshTokenCannotCreateAdminSession() throws Exception {
        account(Role.USER, AccountStatus.ACTIVE);
        String token = tokens.issue(userId);
        mvc.perform(post("/api/v1/admin/auth/refresh").cookie(csrf, new Cookie("refresh_token", token))
                        .header("X-XSRF-TOKEN", csrf.getValue()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ADMIN_REQUIRED"))
                .andExpect(cookie().doesNotExist("access_token"));
    }

    @Test
    void invalidFieldsReturn400() throws Exception {
        login("short").andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.password").isString());
    }
}
