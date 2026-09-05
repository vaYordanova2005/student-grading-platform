package com.markly.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class JwtService {

    /** Claim carrying {@code User#getTokenVersion()}; see {@link JwtAuthenticationFilter}. */
    public static final String TOKEN_VERSION_CLAIM = "tv";
    public static final String ROLE_CLAIM = "role";

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String issueToken(String username, String role, int tokenVersion) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .id(UUID.randomUUID().toString())
                .claim(ROLE_CLAIM, role)
                .claim(TOKEN_VERSION_CLAIM, tokenVersion)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMinutes * 60)))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * The anti-CSRF value handed to the SPA alongside the httpOnly session
     * cookie. Deriving it from the token's {@code jti} keeps the server
     * stateless while still binding it to one session: a token stolen from
     * another tab is useless without the matching header, and the browser
     * cannot produce this value on its own during a cross-site request
     * because it never gets to read the cookie.
     */
    public String csrfTokenFor(Claims claims) {
        return csrfTokenFor(claims.getId());
    }

    public String csrfTokenFor(String tokenId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            return HexFormat.of().formatHex(mac.doFinal(("csrf:" + tokenId).getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException ex) {
            throw new IllegalStateException("Cannot derive CSRF token", ex);
        }
    }

    public boolean csrfTokenMatches(Claims claims, String presented) {
        if (presented == null || claims.getId() == null) {
            return false;
        }
        return MessageDigest.isEqual(
                csrfTokenFor(claims).getBytes(StandardCharsets.UTF_8),
                presented.getBytes(StandardCharsets.UTF_8));
    }

    public long expirationSeconds() {
        return expirationMinutes * 60;
    }
}
