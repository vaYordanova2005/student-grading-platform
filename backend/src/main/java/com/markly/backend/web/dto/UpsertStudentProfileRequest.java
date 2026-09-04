package com.markly.backend.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The {@code @Size} limits mirror the column widths in V5 exactly — without
 * them an over-long value reaches the database and comes back as a 500 from
 * the catch-all handler instead of a 400 the admin can act on. The semester
 * bounds match {@link CreateGradeRequest}: a profile the student sees should
 * not be able to claim semester -5 or 999.
 */
public record UpsertStudentProfileRequest(
        @NotBlank(message = "Имейлът на ученика е задължителен") String studentUsername,
        @Size(max = 100, message = "ОКС е твърде дълго") String degreeLevel,
        @Size(max = 50, message = "Факултетният номер е твърде дълъг") String facultyNumber,
        @Size(max = 255, message = "Факултетът е твърде дълъг") String faculty,
        @Size(max = 255, message = "Специалността е твърде дълга") String specialty,
        @Size(max = 100, message = "Видът обучение е твърде дълъг") String studyMode,
        @Size(max = 255, message = "Специализацията е твърде дълга") String specialization,
        @Size(max = 50, message = "Групата е твърде дълга") String groupNumber,
        @Size(max = 100, message = "Видът прием е твърде дълъг") String admissionType,
        @Size(max = 100, message = "Състоянието е твърде дълго") String status,
        @Min(value = 1, message = "Записаният семестър трябва да е между 1 и 8")
        @Max(value = 8, message = "Записаният семестър трябва да е между 1 и 8") Integer enrolledSemester,
        @Min(value = 0, message = "Завереният семестър трябва да е между 0 и 8")
        @Max(value = 8, message = "Завереният семестър трябва да е между 0 и 8") Integer completedSemester,
        @Size(max = 50, message = "Потокът е твърде дълъг") String stream
) {
}
