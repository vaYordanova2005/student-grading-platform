package com.markly.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registrar-style info about a student (faculty number, specialty, group,
 * enrollment status, etc.) — separate from {@link User} since it's optional,
 * admin-managed, and only meaningful for the STUDENT role.
 */
@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private User student;

    private String degreeLevel;
    private String facultyNumber;
    private String faculty;
    private String specialty;
    private String studyMode;
    private String specialization;
    private String groupNumber;
    private String admissionType;
    private String status;
    private Integer enrolledSemester;
    private Integer completedSemester;
    private String stream;
    private String personalEmail;

    public StudentProfile(User student) {
        this.student = student;
    }
}
