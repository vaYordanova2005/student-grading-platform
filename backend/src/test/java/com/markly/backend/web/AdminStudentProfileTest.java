package com.markly.backend.web;

import com.markly.backend.domain.Role;
import com.markly.backend.domain.User;
import com.markly.backend.repository.UserRepository;
import com.markly.backend.security.AppUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Profile fields are written by an admin but read straight off the student's
 * profile page, so an out-of-range semester or an over-long faculty number has
 * to come back as a 400 the admin can act on — not be stored, and not turn
 * into a 500 when the database rejects a value wider than its column.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminStudentProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User admin;
    private String studentUsername;

    @BeforeEach
    void setUp() {
        admin = userRepository.save(
                new User("admin-" + UUID.randomUUID(), "{noop}irrelevant", Role.ADMIN));
        studentUsername = "student-" + UUID.randomUUID() + "@uni-sofia.bg";
        userRepository.save(new User(studentUsername, "{noop}irrelevant", Role.STUDENT));
    }

    private String body(String enrolledSemester, String facultyNumber) {
        return "{\"studentUsername\":\"" + studentUsername + "\","
                + "\"facultyNumber\":\"" + facultyNumber + "\","
                + "\"enrolledSemester\":" + enrolledSemester + "}";
    }

    @Test
    void acceptsAProfileWithinTheAllowedRanges() throws Exception {
        mockMvc.perform(put("/api/admin/students/profile")
                        .with(user(new AppUserPrincipal(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("8", "121001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrolledSemester").value(8))
                .andExpect(jsonPath("$.facultyNumber").value("121001"));
    }

    @Test
    void rejectsAnEnrolledSemesterAboveEight() throws Exception {
        mockMvc.perform(put("/api/admin/students/profile")
                        .with(user(new AppUserPrincipal(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("999", "121001")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Записаният семестър трябва да е между 1 и 8"));
    }

    @Test
    void rejectsANegativeEnrolledSemester() throws Exception {
        mockMvc.perform(put("/api/admin/students/profile")
                        .with(user(new AppUserPrincipal(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("-5", "121001")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsAFacultyNumberLongerThanItsColumn() throws Exception {
        mockMvc.perform(put("/api/admin/students/profile")
                        .with(user(new AppUserPrincipal(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("1", "1".repeat(51))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Факултетният номер е твърде дълъг"));
    }
}
