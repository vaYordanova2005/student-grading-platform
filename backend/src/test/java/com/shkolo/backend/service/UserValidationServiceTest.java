package com.shkolo.backend.service;

import com.shkolo.backend.domain.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserValidationServiceTest {

    private final UserValidationService service = new UserValidationService();

    @Test
    void acceptsValidTeacher() {
        assertDoesNotThrow(() -> service.validate(Role.TEACHER, "ivanov@tu-sofia.bg", "secret"));
    }

    @Test
    void rejectsTeacherWithBadEmailDomain() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validate(Role.TEACHER, "ivanov@gmail.com", "secret"));
    }

    @Test
    void rejectsTeacherWithShortPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validate(Role.TEACHER, "ivanov@tu-sofia.bg", "ab"));
    }

    @Test
    void acceptsValidStudent() {
        assertDoesNotThrow(() -> service.validate(Role.STUDENT, "123456789", "1234567890"));
    }

    @Test
    void rejectsStudentWithBadFacultyNumber() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validate(Role.STUDENT, "12345", "1234567890"));
    }

    @Test
    void rejectsStudentWithBadEgn() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validate(Role.STUDENT, "123456789", "123"));
    }

    @Test
    void rejectsAdminRole() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validate(Role.ADMIN, "admin", "password"));
    }
}
