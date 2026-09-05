package com.markly.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Behind a proxy {@code getRemoteAddr()} is the proxy, so the client is read
 * from {@code X-Forwarded-For} — counting from the <em>right</em>, not the
 * left. Proxies append to this header rather than replacing it, so a client
 * that sends {@code X-Forwarded-For: 1.2.3.4} itself ends up as
 * {@code 1.2.3.4, <real address>}: the leftmost entries are whatever the
 * caller chose, and using them would let an attacker mint a fresh rate-limit
 * bucket on every request.
 *
 * <p>How far from the right to look depends on how many proxies actually sit
 * in front of the app, which is deployment configuration rather than
 * something this code can know — hence {@code app.security.trusted-proxy-hops}
 * (1 for Render alone). Put another proxy in front, say Cloudflare, without
 * raising it and every user collapses into a single bucket keyed by the
 * Render ingress; raise it too far and the entry becomes caller-controlled
 * again.
 *
 * <p>No header parsing can tell a request that travelled the whole chain from
 * one that skipped it: with {@code trusted-proxy-hops=2}, an attacker who
 * reaches the origin directly and sends a single forged entry produces a
 * header of exactly two entries, and the forged one is the one picked here.
 * Only an origin lock — the origin accepting traffic solely from the CDN —
 * closes that, so it has to be set up together with raising this value.
 *
 * <p>The value is only used for rate-limit bucketing and audit lines — never
 * for authorization.
 */
@Component
public class ClientIpResolver {

    private final int trustedProxyHops;

    public ClientIpResolver(@Value("${app.security.trusted-proxy-hops}") int trustedProxyHops) {
        if (trustedProxyHops < 1) {
            throw new IllegalArgumentException("app.security.trusted-proxy-hops must be at least 1");
        }
        this.trustedProxyHops = trustedProxyHops;
    }

    public String resolve(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] hops = forwarded.split(",");
            // A header with fewer entries than expected means the request did
            // not come through the full proxy chain. Fall back to the
            // rightmost entry, not the leftmost: entries are appended, so the
            // right end is the part closest to whatever proxy actually saw the
            // connection, while the left end is entirely caller-controlled.
            int index = hops.length - trustedProxyHops;
            String clientHop = hops[index < 0 ? hops.length - 1 : index].trim();
            if (!clientHop.isEmpty()) {
                return clientHop;
            }
        }
        String remote = request.getRemoteAddr();
        return remote == null ? "unknown" : remote;
    }
}
