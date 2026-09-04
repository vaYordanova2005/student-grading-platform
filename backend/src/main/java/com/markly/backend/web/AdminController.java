package com.markly.backend.web;

import com.markly.backend.domain.Role;
import com.markly.backend.domain.StudentProfile;
import com.markly.backend.domain.User;
import com.markly.backend.repository.StudentProfileRepository;
import com.markly.backend.repository.UserRepository;
import com.markly.backend.service.UserValidationService;
import com.markly.backend.web.dto.CreateUserRequest;
import com.markly.backend.web.dto.StudentProfileResponse;
import com.markly.backend.web.dto.UpsertStudentProfileRequest;
import com.markly.backend.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final UserValidationService userValidationService;
    private final PasswordEncoder passwordEncoder;
    private final StudentProfileRepository studentProfileRepository;

    public AdminController(
            UserRepository userRepository,
            UserValidationService userValidationService,
            PasswordEncoder passwordEncoder,
            StudentProfileRepository studentProfileRepository) {
        this.userRepository = userRepository;
        this.userValidationService = userValidationService;
        this.passwordEncoder = passwordEncoder;
        this.studentProfileRepository = studentProfileRepository;
    }

    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        Role role = request.role();
        userValidationService.validate(role, request.username(), request.password());

        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new IllegalArgumentException("Потребителското име вече съществува");
        }

        User user = new User(request.username(), passwordEncoder.encode(request.password()), role);
        return UserResponse.from(userRepository.save(user));
    }

    @GetMapping("/students/profile")
    public StudentProfileResponse getStudentProfile(@RequestParam String username) {
        User student = findStudent(username);
        return studentProfileRepository.findByStudent(student)
                .map(profile -> StudentProfileResponse.from(profile, student.getUsername()))
                .orElseGet(() -> StudentProfileResponse.empty(student.getUsername()));
    }

    @PutMapping("/students/profile")
    public StudentProfileResponse upsertStudentProfile(@Valid @RequestBody UpsertStudentProfileRequest request) {
        User student = findStudent(request.studentUsername());
        StudentProfile profile = studentProfileRepository.findByStudent(student)
                .orElseGet(() -> new StudentProfile(student));

        profile.setDegreeLevel(request.degreeLevel());
        profile.setFacultyNumber(request.facultyNumber());
        profile.setFaculty(request.faculty());
        profile.setSpecialty(request.specialty());
        profile.setStudyMode(request.studyMode());
        profile.setSpecialization(request.specialization());
        profile.setGroupNumber(request.groupNumber());
        profile.setAdmissionType(request.admissionType());
        profile.setStatus(request.status());
        profile.setEnrolledSemester(request.enrolledSemester());
        profile.setCompletedSemester(request.completedSemester());
        profile.setStream(request.stream());
        profile.setPersonalEmail(request.personalEmail());

        return StudentProfileResponse.from(studentProfileRepository.save(profile), student.getUsername());
    }

    private User findStudent(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .filter(u -> u.getRole() == Role.STUDENT)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Няма ученик с такъв имейл"));
    }
}
