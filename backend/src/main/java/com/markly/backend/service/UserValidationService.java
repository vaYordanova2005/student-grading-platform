package com.markly.backend.service;

import com.markly.backend.domain.Role;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserValidationService {

    private static final Pattern TEACHER_EMAIL = Pattern.compile("^[a-z]+@uni-sofia\\.bg$");
    private static final Pattern STUDENT_FACULTY_NUMBER = Pattern.compile("^\\d{9}$");
    private static final Pattern STUDENT_EGN = Pattern.compile("^\\d{10}$");

    public void validate(Role role, String username, String password) {
        switch (role) {
            case TEACHER -> validateTeacher(username, password);
            case STUDENT -> validateStudent(username, password);
            case ADMIN -> throw new IllegalArgumentException("Администраторски акаунти не могат да се създават през този endpoint");
        }
    }

    private void validateTeacher(String username, String password) {
        if (!TEACHER_EMAIL.matcher(username).matches()) {
            throw new IllegalArgumentException("Потребителското име на учителя трябва да е @uni-sofia.bg имейл");
        }
        if (password.length() < 5) {
            throw new IllegalArgumentException("Паролата на учителя трябва да е поне 5 символа");
        }
    }

    private void validateStudent(String username, String password) {
        if (!STUDENT_FACULTY_NUMBER.matcher(username).matches()) {
            throw new IllegalArgumentException("Потребителското име на ученика (факултетен номер) трябва да е точно 9 цифри");
        }
        if (!STUDENT_EGN.matcher(password).matches()) {
            throw new IllegalArgumentException("Паролата на ученика (ЕГН) трябва да е точно 10 цифри");
        }
    }
}
