package com.shkolo.backend.web.dto;

import com.shkolo.backend.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotNull Role role,
        @NotBlank String username,
        @NotBlank String password
) {
}
