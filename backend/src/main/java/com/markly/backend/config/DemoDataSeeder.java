package com.markly.backend.config;

import com.markly.backend.domain.Grade;
import com.markly.backend.domain.Role;
import com.markly.backend.domain.User;
import com.markly.backend.repository.GradeRepository;
import com.markly.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * Seeds 8 teachers, 8 subjects, 20 students, and a spread of grades so the
 * dashboards have real data to render. Skips itself once the first demo
 * teacher already exists, so it only ever runs once per database.
 */
@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private static final String DEMO_PASSWORD = "password12345";
    private static final int TEACHER_COUNT = 8;
    private static final int STUDENT_COUNT = 20;
    private static final long RANDOM_SEED = 42L;

    private static final List<String> SUBJECTS = List.of(
            "Математически анализ",
            "Линейна алгебра",
            "Програмиране",
            "Дискретни структури",
            "Обща физика",
            "Електротехника",
            "Бази от данни",
            "Компютърни мрежи"
    );

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-demo-data.enabled:true}")
    private boolean enabled;

    public DemoDataSeeder(UserRepository userRepository, GradeRepository gradeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!enabled || userRepository.existsByUsernameIgnoreCase("teacher1@test.com")) {
            return;
        }

        for (int i = 1; i <= TEACHER_COUNT; i++) {
            String username = "teacher" + i + "@test.com";
            userRepository.save(new User(username, passwordEncoder.encode(DEMO_PASSWORD), Role.TEACHER));
        }

        Random random = new Random(RANDOM_SEED);
        int gradeCount = 0;
        for (int i = 1; i <= STUDENT_COUNT; i++) {
            String username = "student" + i + "@test.com";
            User student = userRepository.save(new User(username, passwordEncoder.encode(DEMO_PASSWORD), Role.STUDENT));

            for (String subject : SUBJECTS) {
                int gradesForSubject = 2 + random.nextInt(2);
                for (int g = 0; g < gradesForSubject; g++) {
                    int semester = g % 2 == 0 ? 1 : 2;
                    gradeRepository.save(new Grade(student, subject, semester, weightedGrade(random)));
                    gradeCount++;
                }
            }
        }

        log.info("Seeded demo data: {} teachers (teacher1..{}@test.com), {} students (student1..{}@test.com), "
                        + "password '{}' for all, {} grades across {} subjects.",
                TEACHER_COUNT, TEACHER_COUNT, STUDENT_COUNT, STUDENT_COUNT, DEMO_PASSWORD, gradeCount, SUBJECTS.size());
    }

    private int weightedGrade(Random random) {
        int roll = random.nextInt(100);
        if (roll < 5) return 2;
        if (roll < 20) return 3;
        if (roll < 45) return 4;
        if (roll < 75) return 5;
        return 6;
    }
}
