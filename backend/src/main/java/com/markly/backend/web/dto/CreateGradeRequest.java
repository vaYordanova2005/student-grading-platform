package com.markly.backend.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateGradeRequest(
        @NotBlank String studentUsername,
        @NotBlank String subject,
        @Min(1) @Max(2) int semester,
        @Min(2) @Max(6) int grade
) {
}
