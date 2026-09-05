package com.markly.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The rate limiter buckets on whatever this returns, so an attacker who can
 * steer it gets an unlimited number of buckets and the per-IP limit stops
 * existing. The forgery cases below are the ones that matter.
 */
class ClientIpResolverTest {

    /** Stands in for a CDN's published ranges. */
    private static final String CDN_NETWORK = "203.0.113.0/24";

    private String resolve(String trustedProxies, String forwardedFor, String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddr);
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }
        return new ClientIpResolver(trustedProxies).resolve(request);
    }

    @Test
    void fallsBackToTheRemoteAddressWithoutTheHeader() {
        assertThat(resolve("", null, "198.51.100.7")).isEqualTo("198.51.100.7");
    }

    @Test
    void takesTheEntryTheProxyAppendedRatherThanTheOneTheCallerSent() {
        assertThat(resolve("", "1.2.3.4, 198.51.100.7", "10.0.0.1")).isEqualTo("198.51.100.7");
    }

    @Test
    void skipsEntriesWrittenByATrustedProxy() {
        assertThat(resolve(CDN_NETWORK, "198.51.100.7, 203.0.113.9", "10.0.0.1"))
                .isEqualTo("198.51.100.7");
    }

    /**
     * The case a hop count cannot handle: the attacker skips the CDN, so the
     * header is the length a two-proxy deployment expects, but the last entry
     * is the attacker's own address rather than the CDN's — and that is what
     * they get bucketed on, no matter what they put to the left of it.
     */
    @Test
    void bucketsOnTheRealAddressWhenTheTrustedProxyIsBypassed() {
        assertThat(resolve(CDN_NETWORK, "1.2.3.4, 198.51.100.7", "198.51.100.7"))
                .isEqualTo("198.51.100.7");
    }

    @Test
    void treatsAMalformedEntryAsTheClientInsteadOfSkippingPastIt() {
        assertThat(resolve(CDN_NETWORK, "1.2.3.4, not-an-ip", "10.0.0.1")).isEqualTo("not-an-ip");
    }
}
