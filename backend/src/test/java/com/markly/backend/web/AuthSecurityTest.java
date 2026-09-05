package com.markly.backend.web;

import com.markly.backend.domain.Role;
import com.markly.backend.domain.User;
import com.markly.backend.repository.UserRepository;
import com.markly.backend.security.AuthCookieService;
import com.markly.backend.security.LoginAttemptService;
import com.markly.backend.security.LoginRateLimitFilter;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers the login path's defences end to end: brute-force limits, account
 * state, the httpOnly session cookie and its CSRF token, and revocation of an
 * already-issued token. These are the cases where a regression is silent —
 * everything keeps working for a legitimate user while the protection is gone.
 *
 * <p>Every test uses its own {@code X-Forwarded-For} address, because the
 * per-IP counter in {@link LoginRateLimitFilter} is a singleton shared by the
 * whole test context and would otherwise leak between tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityTest {

    private static final String PASSWORD = "Silna-Parola1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User student;

    @BeforeEach
    void setUp() {
        student = userRepository.save(new User(
                "student-" + UUID.randomUUID() + "@uni-sofia.bg",
                passwordEncoder.encode(PASSWORD),
                Role.STUDENT));
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteById(student.getId());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder loginRequest(
            String password, String clientIp) {
        return post("/api/auth/login")
                .header("X-Forwarded-For", clientIp)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + student.getUsername() + "\",\"password\":\"" + password + "\"}");
    }

    @Test
    void issuesTheTokenAsAnHttpOnlyCookieAndNeverInTheBody() throws Exception {
        MvcResult result = mockMvc.perform(loginRequest(PASSWORD, "10.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly(AuthCookieService.COOKIE_NAME, true))
                .andExpect(jsonPath("$.username").value(student.getUsername()))
                .andExpect(jsonPath("$.csrfToken").isNotEmpty())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andReturn();

        assertThat(result.getResponse().getHeader(HttpHeaders.SET_COOKIE)).contains("HttpOnly");
    }

    @Test
    void locksTheAccountAfterTooManyFailedAttempts() throws Exception {
        for (int i = 0; i < LoginAttemptService.MAX_FAILED_ATTEMPTS; i++) {
            mockMvc.perform(loginRequest("Grehsna-Parola9", "10.0.0.2"))
                    .andExpect(status().isUnauthorized());
        }

        // Even the correct password is refused once the lockout is in place.
        mockMvc.perform(loginRequest(PASSWORD, "10.0.0.2"))
                .andExpect(status().isLocked());
    }

    @Test
    void rateLimitsRepeatedAttemptsFromTheSameAddress() throws Exception {
        for (int i = 0; i < LoginRateLimitFilter.MAX_ATTEMPTS_PER_WINDOW; i++) {
            mockMvc.perform(loginRequest("Grehsna-Parola9", "10.0.0.3"));
        }

        mockMvc.perform(loginRequest("Grehsna-Parola9", "10.0.0.3"))
                .andExpect(status().isTooManyRequests());
    }

    /**
     * The bucket has to key off the address the proxy appended, not the one
     * the caller put in front of it — otherwise a fresh header value per
     * request buys an attacker an unlimited number of attempts.
     */
    @Test
    void cannotEscapeTheRateLimitByForgingAForwardedForHeader() throws Exception {
        for (int i = 0; i < LoginRateLimitFilter.MAX_ATTEMPTS_PER_WINDOW; i++) {
            mockMvc.perform(loginRequest("Grehsna-Parola9", "1.2.3." + i + ", 10.0.0.8"));
        }

        mockMvc.perform(loginRequest("Grehsna-Parola9", "9.9.9.9, 10.0.0.8"))
                .andExpect(status().isTooManyRequests());
    }

    /**
     * A shared NAT address must not be able to run itself out of the quota by
     * signing in normally.
     */
    @Test
    void successfulLoginsDoNotCountTowardsTheRateLimit() throws Exception {
        for (int i = 0; i < LoginRateLimitFilter.MAX_ATTEMPTS_PER_WINDOW + 3; i++) {
            mockMvc.perform(loginRequest(PASSWORD, "10.0.0.9"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void changingThePasswordInvalidatesTheOtherSessionsButNotThisOne() throws Exception {
        MvcResult first = login("10.0.0.10");
        Cookie otherSession = first.getResponse().getCookie(AuthCookieService.COOKIE_NAME);

        MvcResult second = login("10.0.0.10");
        Cookie currentSession = second.getResponse().getCookie(AuthCookieService.COOKIE_NAME);

        MvcResult changed = mockMvc.perform(post("/api/auth/password")
                        .cookie(currentSession)
                        .header(AuthCookieService.CSRF_HEADER, csrfTokenOf(second))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"" + PASSWORD
                                + "\",\"newPassword\":\"Nova-Parola7\"}"))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get("/api/auth/me").cookie(otherSession))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me")
                        .cookie(changed.getResponse().getCookie(AuthCookieService.COOKIE_NAME)))
                .andExpect(status().isOk());
        mockMvc.perform(loginRequest("Nova-Parola7", "10.0.0.10"))
                .andExpect(status().isOk());
    }

    @Test
    void refusesAWeakNewPasswordAndAWrongCurrentOne() throws Exception {
        MvcResult session = login("10.0.0.11");
        Cookie cookie = session.getResponse().getCookie(AuthCookieService.COOKIE_NAME);
        String csrfToken = csrfTokenOf(session);

        mockMvc.perform(post("/api/auth/password")
                        .cookie(cookie)
                        .header(AuthCookieService.CSRF_HEADER, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"kratka1\"}"))
                .andExpect(status().isBadRequest());

        // A typo in the current password must not read as an expired session,
        // or the SPA would log the user out over it.
        mockMvc.perform(post("/api/auth/password")
                        .cookie(cookie)
                        .header(AuthCookieService.CSRF_HEADER, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"Grehsna-Parola9\",\"newPassword\":\"Nova-Parola7\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refusesADeactivatedAccount() throws Exception {
        student.setEnabled(false);
        userRepository.save(student);

        mockMvc.perform(loginRequest(PASSWORD, "10.0.0.4"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsAStateChangingRequestWithoutTheCsrfToken() throws Exception {
        Cookie sessionCookie = login("10.0.0.5").getResponse().getCookie(AuthCookieService.COOKIE_NAME);

        mockMvc.perform(post("/api/auth/logout").cookie(sessionCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutInvalidatesTheAlreadyIssuedToken() throws Exception {
        MvcResult loginResult = login("10.0.0.6");
        Cookie sessionCookie = loginResult.getResponse().getCookie(AuthCookieService.COOKIE_NAME);
        String csrfToken = csrfTokenOf(loginResult);

        mockMvc.perform(get("/api/auth/me").cookie(sessionCookie))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(sessionCookie)
                        .header(AuthCookieService.CSRF_HEADER, csrfToken))
                .andExpect(status().isNoContent());

        // The very same cookie is now worthless, even though the JWT inside it
        // has not expired.
        mockMvc.perform(get("/api/student/grades").cookie(sessionCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void answersWithACleanUnauthorizedWhenTheUserBehindTheTokenIsGone() throws Exception {
        Cookie sessionCookie = login("10.0.0.7").getResponse().getCookie(AuthCookieService.COOKIE_NAME);
        userRepository.deleteById(student.getId());

        mockMvc.perform(get("/api/student/grades").cookie(sessionCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").isNotEmpty());

        // tearDown() would otherwise fail on the already-deleted row.
        student = userRepository.save(new User(
                student.getUsername(), passwordEncoder.encode(PASSWORD), Role.STUDENT));
    }

    private MvcResult login(String clientIp) throws Exception {
        return mockMvc.perform(loginRequest(PASSWORD, clientIp))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String csrfTokenOf(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return body.replaceAll(".*\"csrfToken\"\s*:\s*\"([^\"]+)\".*", "$1");
    }
}
