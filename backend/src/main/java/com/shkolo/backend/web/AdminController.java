package com.shkolo.backend.web;

import com.shkolo.backend.domain.Role;
import com.shkolo.backend.domain.User;
import com.shkolo.backend.repository.UserRepository;
import com.shkolo.backend.service.UserValidationService;
import com.shkolo.backend.web.dto.CreateUserRequest;
import com.shkolo.backend.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final UserValidationService userValidationService;
    private final PasswordEncoder passwordEncoder;

    public AdminController(
            UserRepository userRepository,
            UserValidationService userValidationService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userValidationService = userValidationService;
        this.passwordEncoder = passwordEncoder;
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
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User(request.username(), passwordEncoder.encode(request.password()), role);
        return UserResponse.from(userRepository.save(user));
    }
}
