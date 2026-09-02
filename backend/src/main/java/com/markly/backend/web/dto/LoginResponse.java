package com.markly.backend.web.dto;

public record LoginResponse(String token, String username, String role) {
}
