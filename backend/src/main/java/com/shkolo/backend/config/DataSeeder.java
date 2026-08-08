package com.shkolo.backend.config;

import com.shkolo.backend.domain.Role;
import com.shkolo.backend.domain.User;
import com.shkolo.backend.repository.UserRepository;
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

    @Value("${app.seed-admin.username}")
    private String seedAdminUsername;

    @Value("${app.seed-admin.password}")
    private String seedAdminPassword;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        User admin = new User(seedAdminUsername, passwordEncoder.encode(seedAdminPassword), Role.ADMIN);
        userRepository.save(admin);
        log.info("Seeded initial admin user '{}' — change the password after first login.", seedAdminUsername);
    }
}
