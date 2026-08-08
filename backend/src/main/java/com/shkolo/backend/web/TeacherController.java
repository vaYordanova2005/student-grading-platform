package com.shkolo.backend.web;

import com.shkolo.backend.domain.Grade;
import com.shkolo.backend.domain.Role;
import com.shkolo.backend.domain.User;
import com.shkolo.backend.repository.GradeRepository;
import com.shkolo.backend.repository.UserRepository;
import com.shkolo.backend.web.dto.CreateGradeRequest;
import com.shkolo.backend.web.dto.GradeResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public GradeResponse addGrade(@Valid @RequestBody CreateGradeRequest request) {
        User student = userRepository.findByUsernameIgnoreCase(request.studentUsername())
                .filter(u -> u.getRole() == Role.STUDENT)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid faculty number"));

        Grade grade = new Grade(student, request.subject(), request.semester(), request.grade());
        return GradeResponse.from(gradeRepository.save(grade));
    }
}
