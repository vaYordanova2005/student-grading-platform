package com.markly.backend.config;

import com.markly.backend.domain.Role;
import com.markly.backend.domain.User;
import com.markly.backend.repository.UserRepository;
import com.markly.backend.service.UserValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidationService userValidationService;
    private final String seedAdminUsername;
    private final String seedAdminPassword;

    public DataSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserValidationService userValidationService,
            @Value("${app.seed-admin.username}") String seedAdminUsername,
            @Value("${app.seed-admin.password}") String seedAdminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userValidationService = userValidationService;
        this.seedAdminUsername = seedAdminUsername;
        this.seedAdminPassword = seedAdminPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsernameIgnoreCase(seedAdminUsername)) {
            return;
        }
        // The admin is the account worth the most to an attacker, and it is
        // the one account created outside the admin panel — without this it
        // would be the only one exempt from the password policy.
        try {
            userValidationService.validatePassword(seedAdminUsername, seedAdminPassword);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "SEED_ADMIN_PASSWORD does not meet the password policy: " + ex.getMessage(), ex);
        }
        User admin = new User(seedAdminUsername, passwordEncoder.encode(seedAdminPassword), Role.ADMIN);
        userRepository.save(admin);
        log.info("Seeded initial admin user '{}'.", seedAdminUsername);
    }
}
