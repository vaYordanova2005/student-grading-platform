package com.markly.backend.web;

import com.markly.backend.domain.Grade;
import com.markly.backend.domain.Role;
import com.markly.backend.domain.User;
import com.markly.backend.repository.GradeRepository;
import com.markly.backend.repository.UserRepository;
import com.markly.backend.security.AppUserPrincipal;
import com.markly.backend.web.dto.CreateGradeRequest;
import com.markly.backend.web.dto.GradeResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;

    public TeacherController(UserRepository userRepository, GradeRepository gradeRepository) {
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
    }

    @PostMapping("/grades")
    @ResponseStatus(HttpStatus.CREATED)
    public GradeResponse addGrade(
            @Valid @RequestBody CreateGradeRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        User student = userRepository.findByUsernameIgnoreCase(request.studentUsername())
                .filter(u -> u.getRole() == Role.STUDENT)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid faculty number"));

        Grade grade = new Grade(student, principal.getUser(), request.subject(), request.semester(), request.grade());
        return GradeResponse.from(gradeRepository.save(grade));
    }
}
