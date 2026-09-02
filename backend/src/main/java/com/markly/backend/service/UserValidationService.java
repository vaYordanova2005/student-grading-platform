package com.markly.backend.service;

import com.markly.backend.domain.Role;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserValidationService {

    private static final Pattern TEACHER_EMAIL = Pattern.compile("^[a-z]+@tu-sofia\\.bg$");
    private static final Pattern STUDENT_FACULTY_NUMBER = Pattern.compile("^\\d{9}$");
    private static final Pattern STUDENT_EGN = Pattern.compile("^\\d{10}$");

    public void validate(Role role, String username, String password) {
        switch (role) {
            case TEACHER -> validateTeacher(username, password);
            case STUDENT -> validateStudent(username, password);
            case ADMIN -> throw new IllegalArgumentException("Admin accounts cannot be created through this endpoint");
        }
    }

    private void validateTeacher(String username, String password) {
        if (!TEACHER_EMAIL.matcher(username).matches()) {
            throw new IllegalArgumentException("Teacher username must be an @tu-sofia.bg email address");
        }
        if (password.length() < 5) {
            throw new IllegalArgumentException("Teacher password must be at least 5 characters");
        }
    }

    private void validateStudent(String username, String password) {
        if (!STUDENT_FACULTY_NUMBER.matcher(username).matches()) {
            throw new IllegalArgumentException("Student username (faculty number) must be exactly 9 digits");
        }
        if (!STUDENT_EGN.matcher(password).matches()) {
            throw new IllegalArgumentException("Student password (EGN) must be exactly 10 digits");
        }
    }
}
