package com.markly.backend.web;

import com.markly.backend.domain.CalendarEvent;
import com.markly.backend.domain.CalendarEventType;
import com.markly.backend.repository.CalendarEventRepository;
import com.markly.backend.security.AppUserPrincipal;
import com.markly.backend.web.dto.CalendarEventResponse;
import com.markly.backend.web.dto.CreateCalendarEventRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Every authenticated role can read the calendar; only ADMIN/TEACHER can
 * write to it (see the {@code /api/calendar/**} matchers in SecurityConfig).
 */
@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarEventRepository calendarEventRepository;

    public CalendarController(CalendarEventRepository calendarEventRepository) {
        this.calendarEventRepository = calendarEventRepository;
    }

    @GetMapping("/events")
    public List<CalendarEventResponse> listEvents() {
        return calendarEventRepository.findAllByOrderByStartDateAsc().stream()
                .map(CalendarEventResponse::from)
                .toList();
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public CalendarEventResponse addEvent(
            @Valid @RequestBody CreateCalendarEventRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        boolean isTest = request.type() == CalendarEventType.TEST;
        if (isTest && (request.subject() == null || request.subject().isBlank())) {
            throw new IllegalArgumentException("Предметът е задължителен за тест");
        }
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("Крайната дата трябва да е след началната");
        }

        CalendarEvent event = new CalendarEvent(
                request.type(),
                request.title(),
                request.description(),
                isTest ? request.subject() : null,
                request.startDate(),
                request.endDate(),
                principal.getUser());
        return CalendarEventResponse.from(calendarEventRepository.save(event));
    }

    @DeleteMapping("/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id) {
        if (!calendarEventRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Събитието не е намерено");
        }
        calendarEventRepository.deleteById(id);
    }
}
