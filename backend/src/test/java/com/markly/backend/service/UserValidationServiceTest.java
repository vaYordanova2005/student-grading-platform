package com.markly.backend.service;

import com.markly.backend.domain.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserValidationServiceTest {

    private final UserValidationService service = new UserValidationService();

    @Test
    void acceptsValidTeacher() {
        assertDoesNotThrow(() -> service.validate(Role.TEACHER, "ivanov@uni-sofia.bg", "secret"));
    }

    @Test
    void rejectsTeacherWithBadEmailDomain() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validate(Role.TEACHER, "ivanov@gmail.com", "secret"));
    }

    @Test
    void rejectsTeacherWithShortPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validate(Role.TEACHER, "ivanov@uni-sofia.bg", "ab"));
    }

    @Test
    void acceptsValidStudent() {
        assertDoesNotThrow(() -> service.validate(Role.STUDENT, "student1@test.com", "1234567890"));
    }

    @Test
    void rejectsStudentWithInvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validate(Role.STUDENT, "not-an-email", "1234567890"));
    }

    @Test
    void rejectsStudentWithShortPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validate(Role.STUDENT, "student1@test.com", "123"));
    }

    @Test
    void rejectsAdminRole() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validate(Role.ADMIN, "admin", "password"));
    }
}
