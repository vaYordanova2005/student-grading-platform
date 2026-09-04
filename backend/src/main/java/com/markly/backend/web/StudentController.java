package com.markly.backend.web;

import com.markly.backend.repository.GradeRepository;
import com.markly.backend.repository.StudentProfileRepository;
import com.markly.backend.security.AppUserPrincipal;
import com.markly.backend.web.dto.GradeResponse;
import com.markly.backend.web.dto.StudentProfileResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final GradeRepository gradeRepository;
    private final StudentProfileRepository studentProfileRepository;

    public StudentController(GradeRepository gradeRepository, StudentProfileRepository studentProfileRepository) {
        this.gradeRepository = gradeRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    @GetMapping("/grades")
    public List<GradeResponse> myGrades(@AuthenticationPrincipal AppUserPrincipal principal) {
        return gradeRepository.findByStudentOrderBySemesterAscSubjectAsc(principal.getUser()).stream()
                .map(GradeResponse::from)
                .toList();
    }

    @GetMapping("/profile")
    public StudentProfileResponse myProfile(@AuthenticationPrincipal AppUserPrincipal principal) {
        return studentProfileRepository.findByStudent(principal.getUser())
                .map(profile -> StudentProfileResponse.from(profile, principal.getUser().getUsername()))
                .orElseGet(() -> StudentProfileResponse.empty(principal.getUser().getUsername()));
    }
}
