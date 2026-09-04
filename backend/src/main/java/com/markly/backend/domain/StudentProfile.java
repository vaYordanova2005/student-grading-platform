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

    // Lengths are declared explicitly so the entity agrees with the columns in
    // V5. Left at the JPA default of 255 they would silently disagree with the
    // narrower VARCHAR(50)/VARCHAR(100) columns — a mismatch ddl-auto: validate
    // does not report, since it only checks that the columns exist.
    @Column(length = 100)
    private String degreeLevel;

    @Column(length = 50)
    private String facultyNumber;

    @Column(length = 255)
    private String faculty;

    @Column(length = 255)
    private String specialty;

    @Column(length = 100)
    private String studyMode;

    @Column(length = 255)
    private String specialization;

    @Column(length = 50)
    private String groupNumber;

    @Column(length = 100)
    private String admissionType;

    @Column(length = 100)
    private String status;

    private Integer enrolledSemester;
    private Integer completedSemester;

    @Column(length = 50)
    private String stream;

    public StudentProfile(User student) {
        this.student = student;
    }
}
