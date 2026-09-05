package com.markly.backend.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Behind Render's proxy {@code getRemoteAddr()} is the proxy, so the client is
 * read from {@code X-Forwarded-For} — taking the <em>last</em> entry, not the
 * first. Proxies append to this header rather than replacing it, so a client
 * that sends {@code X-Forwarded-For: 1.2.3.4} itself ends up as
 * {@code 1.2.3.4, <real address>}: the leftmost entry is whatever the caller
 * chose, and using it would let an attacker land in a fresh rate-limit bucket
 * on every request. The rightmost entry is the one Render added and is the
 * only part of the header the caller cannot forge.
 *
 * <p>This assumes exactly one trusted proxy in front of the app, which is how
 * it is deployed; adding another hop would mean skipping one more entry from
 * the right. The value is still only used for rate-limit bucketing and audit
 * lines — never for authorization.
 */
public final class ClientIp {

    private ClientIp() {
    }

    public static String of(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] hops = forwarded.split(",");
            String closestProxyHop = hops[hops.length - 1].trim();
            if (!closestProxyHop.isEmpty()) {
                return closestProxyHop;
            }
        }
        String remote = request.getRemoteAddr();
        return remote == null ? "unknown" : remote;
    }
}
