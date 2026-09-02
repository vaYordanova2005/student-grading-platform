package com.markly.backend.repository;

import com.markly.backend.domain.Grade;
import com.markly.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentOrderBySemesterAscSubjectAsc(User student);
}
