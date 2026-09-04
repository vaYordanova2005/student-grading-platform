package com.markly.backend.repository;

import com.markly.backend.domain.StudentProfile;
import com.markly.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByStudent(User student);
}
