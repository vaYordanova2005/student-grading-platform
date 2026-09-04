package com.markly.backend.web.dto;

import com.markly.backend.domain.CalendarEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateCalendarEventRequest(
        @NotNull(message = "Типът е задължителен") CalendarEventType type,
        @NotBlank(message = "Заглавието е задължително") String title,
        String description,
        String subject,
        @NotNull(message = "Началната дата е задължителна") LocalDate startDate,
        LocalDate endDate
) {
}
