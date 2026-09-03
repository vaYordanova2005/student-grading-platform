package com.markly.backend.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateGradeRequest(
        @NotBlank(message = "Факултетният номер е задължителен") String studentUsername,
        @NotBlank(message = "Предметът е задължителен") String subject,
        @Min(value = 1, message = "Семестърът трябва да е 1 или 2")
        @Max(value = 2, message = "Семестърът трябва да е 1 или 2") int semester,
        @Min(value = 2, message = "Оценката трябва да е между 2 и 6")
        @Max(value = 6, message = "Оценката трябва да е между 2 и 6") int grade
) {
}
