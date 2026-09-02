package com.markly.backend.web.dto;

import com.markly.backend.domain.User;

public record UserResponse(Long id, String username, String role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole().name());
    }
}
