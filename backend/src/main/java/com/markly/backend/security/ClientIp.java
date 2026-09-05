package com.markly.backend.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Behind Render's proxy {@code getRemoteAddr()} is the proxy, so the real
 * client is the first entry of {@code X-Forwarded-For}. That header is
 * attacker-controlled when the app is reached directly, which is why it is
 * only ever used for rate-limit bucketing and audit lines — never for
 * authorization.
 */
public final class ClientIp {

    private ClientIp() {
    }

    public static String of(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String remote = request.getRemoteAddr();
        return remote == null ? "unknown" : remote;
    }
}
