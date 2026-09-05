package com.markly.backend.web.dto;

/**
 * Carries no token: the JWT is set as an httpOnly cookie so page scripts
 * cannot read it. {@code csrfToken} is the value the SPA must echo back in
 * the {@code X-CSRF-Token} header on state-changing calls.
 */
public record LoginResponse(String username, String role, String csrfToken) {
}
