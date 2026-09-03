package com.markly.backend.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Потребителското име е задължително") String username,
        @NotBlank(message = "Паролата е задължителна") String password
) {
}
