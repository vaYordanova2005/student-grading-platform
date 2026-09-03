package com.markly.backend.web.dto;

import com.markly.backend.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotNull(message = "Ролята е задължителна") Role role,
        @NotBlank(message = "Потребителското име е задължително") String username,
        @NotBlank(message = "Паролата е задължителна") String password
) {
}
