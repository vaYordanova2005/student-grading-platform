package com.markly.backend.web.dto;

import com.markly.backend.domain.StudentProfile;

public record StudentProfileResponse(
        String studentUsername,
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
        String stream,
        String personalEmail
) {
    /**
     * {@code username} is passed in explicitly rather than read off
     * {@code profile.getStudent()} — that association is lazy, and the
     * caller already knows which student this is for, so there's no reason
     * to risk a LazyInitializationException fetching it back out.
     */
    public static StudentProfileResponse from(StudentProfile profile, String username) {
        return new StudentProfileResponse(
                username,
                profile.getDegreeLevel(),
                profile.getFacultyNumber(),
                profile.getFaculty(),
                profile.getSpecialty(),
                profile.getStudyMode(),
                profile.getSpecialization(),
                profile.getGroupNumber(),
                profile.getAdmissionType(),
                profile.getStatus(),
                profile.getEnrolledSemester(),
                profile.getCompletedSemester(),
                profile.getStream(),
                profile.getPersonalEmail());
    }

    public static StudentProfileResponse empty(String username) {
        return new StudentProfileResponse(username, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
