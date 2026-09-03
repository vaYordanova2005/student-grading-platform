package com.markly.backend.web.dto;

import com.markly.backend.domain.Grade;

import java.time.Instant;

public record GradeResponse(Long id, String subject, int semester, int grade, Instant createdAt) {
    public static GradeResponse from(Grade grade) {
        return new GradeResponse(
                grade.getId(), grade.getSubject(), grade.getSemester(), grade.getGrade(), grade.getCreatedAt());
    }
}
