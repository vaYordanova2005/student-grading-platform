package com.markly.backend.config;

import com.markly.backend.domain.CalendarEvent;
import com.markly.backend.domain.CalendarEventType;
import com.markly.backend.domain.Grade;
import com.markly.backend.domain.Role;
import com.markly.backend.domain.User;
import com.markly.backend.repository.CalendarEventRepository;
import com.markly.backend.repository.GradeRepository;
import com.markly.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/**
 * Seeds 8 teachers, 8 subjects, 20 students, and a spread of grades so the
 * dashboards have real data to render. Disabled by default — opt in with
 * {@code SEED_DEMO_DATA=true}. Skips itself once the first demo teacher
 * already exists, so it only ever runs once per database. Usernames/passwords
 * are generated to satisfy {@link com.markly.backend.service.UserValidationService}
 * so demo accounts behave exactly like admin-created ones; see README for the
 * shared demo credentials.
 */
@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    static final String DEMO_TEACHER_PASSWORD = "password12345";
    static final String DEMO_STUDENT_PASSWORD = "password12345";

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

    // Teacher usernames must match ^[a-z]+[0-9]*@uni-sofia.bg$.
    private static final List<String> TEACHER_HANDLES = List.of(
            "teacher1", "teacher2", "teacher3", "teacher4",
            "teacher5", "teacher6", "teacher7", "teacher8"
    );

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;

    public DemoDataSeeder(
            UserRepository userRepository,
            GradeRepository gradeRepository,
            CalendarEventRepository calendarEventRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed-demo-data.enabled:false}") boolean enabled) {
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.calendarEventRepository = calendarEventRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled || userRepository.existsByUsernameIgnoreCase(TEACHER_HANDLES.get(0) + "@uni-sofia.bg")) {
            return;
        }

        List<User> teachers = TEACHER_HANDLES.stream()
                .map(handle -> userRepository.save(
                        new User(handle + "@uni-sofia.bg", passwordEncoder.encode(DEMO_TEACHER_PASSWORD), Role.TEACHER)))
                .toList();

        Random random = new Random(RANDOM_SEED);
        int gradeCount = 0;
        for (int i = 1; i <= STUDENT_COUNT; i++) {
            String username = "student" + i + "@uni-sofia.bg";
            User student = userRepository.save(
                    new User(username, passwordEncoder.encode(DEMO_STUDENT_PASSWORD), Role.STUDENT));

            for (int s = 0; s < SUBJECTS.size(); s++) {
                String subject = SUBJECTS.get(s);
                User teacher = teachers.get(s % teachers.size());
                int gradesForSubject = 2 + random.nextInt(2);
                for (int g = 0; g < gradesForSubject; g++) {
                    int semester = g % 2 == 0 ? 1 : 2;
                    gradeRepository.save(new Grade(student, teacher, subject, semester, weightedGrade(random)));
                    gradeCount++;
                }
            }
        }

        seedCalendarEvents(teachers.get(0));

        log.info("Seeded demo data: {} teachers ({}..{}@uni-sofia.bg), {} students (student1..{}@uni-sofia.bg), "
                        + "{} grades across {} subjects. Demo credentials are documented in README.",
                TEACHER_COUNT, TEACHER_HANDLES.get(0), TEACHER_HANDLES.get(TEACHER_HANDLES.size() - 1),
                STUDENT_COUNT, STUDENT_COUNT,
                gradeCount, SUBJECTS.size());
    }

    /**
     * A handful of upcoming tests (one per subject), plus a holiday and a
     * job-fair event, so the calendar isn't empty on a fresh demo database.
     * Attributed to the first demo teacher — which role actually "created"
     * seed data doesn't matter here.
     */
    private void seedCalendarEvents(User creator) {
        LocalDate today = LocalDate.now();
        for (int s = 0; s < SUBJECTS.size(); s++) {
            calendarEventRepository.save(new CalendarEvent(
                    CalendarEventType.TEST,
                    "Тест по " + SUBJECTS.get(s),
                    null,
                    SUBJECTS.get(s),
                    today.plusDays(7 + s * 3L),
                    null,
                    creator));
        }
        calendarEventRepository.save(new CalendarEvent(
                CalendarEventType.EVENT,
                "Кариерен ден — фирмени представяния",
                "Компании от бранша представят стажантски и junior позиции.",
                null,
                today.plusDays(14),
                null,
                creator));
        calendarEventRepository.save(new CalendarEvent(
                CalendarEventType.HOLIDAY,
                "Зимна ваканция",
                null,
                null,
                today.plusDays(30),
                today.plusDays(37),
                creator));
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
