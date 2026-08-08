package com.shkolo.backend.repository;

import com.shkolo.backend.domain.Grade;
import com.shkolo.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentOrderBySemesterAscSubjectAsc(User student);
}
