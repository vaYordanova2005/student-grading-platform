package com.markly.backend.service;

import com.markly.backend.domain.Role;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserValidationService {

    private static final Pattern TEACHER_EMAIL = Pattern.compile("^[a-z]+@uni-sofia\\.bg$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

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
        if (!EMAIL.matcher(username).matches()) {
            throw new IllegalArgumentException("Потребителското име на ученика трябва да е валиден имейл адрес");
        }
        if (password.length() < 5) {
            throw new IllegalArgumentException("Паролата на ученика трябва да е поне 5 символа");
        }
    }
}
