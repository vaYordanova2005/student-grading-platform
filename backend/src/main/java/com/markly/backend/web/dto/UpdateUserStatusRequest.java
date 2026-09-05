package com.markly.backend.web.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "Статусът е задължителен") Boolean enabled
) {
}
