package com.markly.backend.config;

import com.markly.backend.domain.Role;
import com.markly.backend.domain.User;
import com.markly.backend.repository.UserRepository;
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
    private final String seedAdminUsername;
    private final String seedAdminPassword;

    public DataSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed-admin.username}") String seedAdminUsername,
            @Value("${app.seed-admin.password}") String seedAdminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedAdminUsername = seedAdminUsername;
        this.seedAdminPassword = seedAdminPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsernameIgnoreCase(seedAdminUsername)) {
            return;
        }
        User admin = new User(seedAdminUsername, passwordEncoder.encode(seedAdminPassword), Role.ADMIN);
        userRepository.save(admin);
        log.info("Seeded initial admin user '{}'.", seedAdminUsername);
    }
}
