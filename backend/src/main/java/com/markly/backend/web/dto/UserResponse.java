package com.markly.backend.web.dto;

import com.markly.backend.domain.User;

import java.time.Instant;

public record UserResponse(Long id, String username, String role, boolean enabled, boolean locked) {
    public static UserResponse from(User user) {
        boolean locked = user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now());
        return new UserResponse(user.getId(), user.getUsername(), user.getRole().name(), user.isEnabled(), locked);
    }
}
