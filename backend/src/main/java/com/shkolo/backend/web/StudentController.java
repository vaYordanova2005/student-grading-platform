package com.shkolo.backend.web;

import com.shkolo.backend.repository.GradeRepository;
import com.shkolo.backend.security.AppUserPrincipal;
import com.shkolo.backend.web.dto.GradeResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final GradeRepository gradeRepository;

    public StudentController(GradeRepository gradeRepository) {
        this.gradeRepository = gradeRepository;
    }

    @GetMapping("/grades")
    public List<GradeResponse> myGrades(@AuthenticationPrincipal AppUserPrincipal principal) {
        return gradeRepository.findByStudentOrderBySemesterAscSubjectAsc(principal.getUser()).stream()
                .map(GradeResponse::from)
                .toList();
    }
}
