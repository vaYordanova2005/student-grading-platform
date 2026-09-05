package com.markly.backend.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Текущата парола е задължителна") String currentPassword,
        @NotBlank(message = "Новата парола е задължителна") String newPassword
) {
}
