package com.markly.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * Keeps the JWT in an httpOnly cookie instead of {@code localStorage}: script
 * running in the page — say, through an XSS hole — can no longer read the
 * token, because the browser attaches it without ever exposing its value.
 * The trade-off is that the browser now sends it automatically, which is what
 * the CSRF header check in {@link JwtAuthenticationFilter} covers.
 *
 * <p>When the SPA and the API are served from different hosts (as they are on
 * Render), the cookie is cross-site and needs {@code SameSite=None; Secure} —
 * hence the two properties rather than hard-coded values, since a local
 * http://localhost setup cannot use {@code Secure}.
 */
@Component
public class AuthCookieService {

    public static final String COOKIE_NAME = "markly_token";
    public static final String CSRF_HEADER = "X-CSRF-Token";

    private final boolean secure;
    private final String sameSite;
    private final long maxAgeSeconds;

    public AuthCookieService(
            @Value("${app.auth.cookie.secure}") boolean secure,
            @Value("${app.auth.cookie.same-site}") String sameSite,
            JwtService jwtService) {
        this.secure = secure;
        this.sameSite = sameSite;
        this.maxAgeSeconds = jwtService.expirationSeconds();
    }

    public ResponseCookie create(String token) {
        return base(token).maxAge(maxAgeSeconds).build();
    }

    /** Same attributes with a zero max-age — anything else leaves the cookie in place. */
    public ResponseCookie clear() {
        return base("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/");
    }

    public Optional<String> readToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(jakarta.servlet.http.Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }
}
