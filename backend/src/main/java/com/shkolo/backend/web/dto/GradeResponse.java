package com.shkolo.backend.web.dto;

import com.shkolo.backend.domain.Grade;

public record GradeResponse(Long id, String subject, int semester, int grade) {
    public static GradeResponse from(Grade grade) {
        return new GradeResponse(grade.getId(), grade.getSubject(), grade.getSemester(), grade.getGrade());
    }
}
