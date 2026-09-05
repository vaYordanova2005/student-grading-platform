package com.markly.backend.service;

import com.markly.backend.domain.Role;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Pattern;

@Service
public class UserValidationService {

    private static final Pattern TEACHER_EMAIL = Pattern.compile("^[a-z]+[0-9]*@uni-sofia\\.bg$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Ten characters with three character classes; the previous five-character
     * minimum with no complexity requirement left accounts guessable in
     * minutes even with the lockout in {@code LoginAttemptService} in place.
     */
    static final int MIN_PASSWORD_LENGTH = 10;

    /**
     * The handful of passwords a guessing attempt starts with. A full
     * breach-corpus check belongs in a service like Have I Been Pwned; this
     * only rules out the values that would otherwise pass the length and
     * complexity rules above (e.g. "Password123").
     */
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password1", "password12", "password123", "password1234", "parola123",
            "qwerty123", "qwertyuiop", "1234567890", "12345678910", "abcd1234",
            "passw0rd1", "adminadmin", "admin12345", "welcome123", "iloveyou1",
            "letmein123", "monkey1234", "football12", "dragon1234", "sunshine1");

    public void validate(Role role, String username, String password) {
        switch (role) {
            case TEACHER -> validateTeacher(username, password);
            case STUDENT -> validateStudent(username, password);
            case ADMIN -> throw new IllegalArgumentException("Администраторски акаунти не могат да се създават през този endpoint");
        }
    }

    private void validateTeacher(String username, String password) {
        if (!TEACHER_EMAIL.matcher(username).matches()) {
            throw new IllegalArgumentException("Потребителското име на учителя трябва да е @uni-sofia.bg имейл");
        }
        validatePassword(username, password);
    }

    private void validateStudent(String username, String password) {
        if (!EMAIL.matcher(username).matches()) {
            throw new IllegalArgumentException("Потребителското име на ученика трябва да е валиден имейл адрес");
        }
        validatePassword(username, password);
    }

    /**
     * Same rules for every role — a weak teacher password is no less of a way
     * in than a weak student one.
     */
    public void validatePassword(String username, String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Паролата трябва да е поне " + MIN_PASSWORD_LENGTH + " символа");
        }
        if (password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isLowerCase)
                || password.chars().noneMatch(Character::isDigit)) {
            throw new IllegalArgumentException(
                    "Паролата трябва да съдържа поне една главна буква, една малка буква и една цифра");
        }
        if (password.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("Паролата не трябва да съдържа интервали");
        }
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            throw new IllegalArgumentException("Тази парола е твърде често използвана");
        }
        // The local part of the email is public knowledge, so a password built
        // out of it is the first thing an attacker tries.
        String localPart = username == null ? "" : username.split("@")[0].toLowerCase();
        if (localPart.length() >= 3 && password.toLowerCase().contains(localPart)) {
            throw new IllegalArgumentException("Паролата не трябва да съдържа потребителското име");
        }
    }
}
