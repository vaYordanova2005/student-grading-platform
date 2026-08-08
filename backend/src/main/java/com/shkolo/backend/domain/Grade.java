package com.shkolo.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "grades")
@Getter
@Setter
@NoArgsConstructor
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private int semester;

    @Column(nullable = false)
    private int grade;

    public Grade(User student, String subject, int semester, int grade) {
        this.student = student;
        this.subject = subject;
        this.semester = semester;
        this.grade = grade;
    }
}
