package com.markly.backend.config;

import com.markly.backend.service.UserValidationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * The seeded accounts are created outside the admin panel and so never pass
 * through {@code AdminController}'s validation. {@code DataSeeder} checks the
 * configured admin password at startup; the demo passwords are constants, so
 * they are checked here instead — a weakened constant would otherwise ship a
 * set of accounts the policy would have rejected.
 */
class SeedPasswordPolicyTest {

    private final UserValidationService service = new UserValidationService();

    @Test
    void demoPasswordsSatisfyThePasswordPolicy() {
        assertDoesNotThrow(() -> service.validatePassword(
                "teacher1@uni-sofia.bg", DemoDataSeeder.DEMO_TEACHER_PASSWORD));
        assertDoesNotThrow(() -> service.validatePassword(
                "student1@uni-sofia.bg", DemoDataSeeder.DEMO_STUDENT_PASSWORD));
    }
}
