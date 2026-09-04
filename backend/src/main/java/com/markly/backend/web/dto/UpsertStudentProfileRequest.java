package com.markly.backend.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpsertStudentProfileRequest(
        @NotBlank(message = "Имейлът на ученика е задължителен") String studentUsername,
        String degreeLevel,
        String facultyNumber,
        String faculty,
        String specialty,
        String studyMode,
        String specialization,
        String groupNumber,
        String admissionType,
        String status,
        Integer enrolledSemester,
        Integer completedSemester,
        String stream
) {
}
