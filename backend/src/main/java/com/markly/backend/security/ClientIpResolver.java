package com.markly.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Behind a proxy {@code getRemoteAddr()} is the proxy, so the client is read
 * from {@code X-Forwarded-For} — walking from the <em>right</em>. Proxies
 * append to this header rather than replacing it, so a client that sends
 * {@code X-Forwarded-For: 1.2.3.4} itself ends up as
 * {@code 1.2.3.4, <real address>}: the left end is whatever the caller chose,
 * and trusting it would let an attacker mint a fresh rate-limit bucket on
 * every request.
 *
 * <p>Which entries to skip is decided by <em>who wrote them</em>, not by how
 * many there are: {@code app.security.trusted-proxies} lists the networks the
 * proxies in front of the app occupy (empty by default — Render appends
 * exactly one entry, so the rightmost one is the client), and the first entry
 * from the right that is not one of them is the client.
 *
 * <p>A hop count would have been simpler but breaks the moment a CDN is added
 * in front: an attacker who bypasses the CDN and reaches the origin directly
 * with one forged entry produces a header of exactly the expected length, and
 * a count-based resolver would trust the forged entry. Matching networks
 * instead, that request's rightmost entry is the attacker's own address —
 * which is not a trusted proxy, so it is what gets used. (An origin lock is
 * still worth having, but it is no longer the only thing standing between an
 * attacker and an unlimited number of rate-limit buckets.)
 *
 * <p>The value is only used for rate-limit bucketing and audit lines — never
 * for authorization.
 */
@Component
public class ClientIpResolver {

    private final List<IpAddressMatcher> trustedProxies;

    public ClientIpResolver(@Value("${app.security.trusted-proxies}") String trustedProxies) {
        this.trustedProxies = Arrays.stream(trustedProxies.split(","))
                .map(String::trim)
                .filter(cidr -> !cidr.isEmpty())
                .map(IpAddressMatcher::new)
                .toList();
    }

    public String resolve(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] hops = forwarded.split(",");
            for (int i = hops.length - 1; i >= 0; i--) {
                String hop = hops[i].trim();
                if (!hop.isEmpty() && !isTrustedProxy(hop)) {
                    return hop;
                }
            }
        }
        String remote = request.getRemoteAddr();
        return remote == null ? "unknown" : remote;
    }

    /**
     * A malformed entry (an attacker is free to put anything in the header)
     * matches nothing and is therefore treated as the client — which is the
     * safe direction: it keeps the forged value in its own bucket instead of
     * letting it skip past the real address to the left.
     */
    private boolean isTrustedProxy(String hop) {
        return trustedProxies.stream().anyMatch(matcher -> {
            try {
                return matcher.matches(hop);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        });
    }
}
