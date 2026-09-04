package com.markly.backend.web.dto;

import com.markly.backend.domain.CalendarEvent;
import com.markly.backend.domain.CalendarEventType;
import com.markly.backend.domain.Role;

import java.time.Instant;
import java.time.LocalDate;

public record CalendarEventResponse(
        Long id,
        CalendarEventType type,
        String title,
        String description,
        String subject,
        LocalDate startDate,
        LocalDate endDate,
        String createdByUsername,
        Role createdByRole,
        Instant createdAt) {
    public static CalendarEventResponse from(CalendarEvent event) {
        return new CalendarEventResponse(
                event.getId(),
                event.getType(),
                event.getTitle(),
                event.getDescription(),
                event.getSubject(),
                event.getStartDate(),
                event.getEndDate(),
                event.getCreatedBy().getUsername(),
                event.getCreatedBy().getRole(),
                event.getCreatedAt());
    }
}
