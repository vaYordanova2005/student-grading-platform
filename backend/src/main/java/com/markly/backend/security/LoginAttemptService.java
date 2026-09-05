package com.markly.backend.security;

import com.markly.backend.domain.User;
import com.markly.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Per-account half of the brute-force defence: counts consecutive failed
 * logins and locks the account for {@link #LOCK_DURATION} once
 * {@link #MAX_FAILED_ATTEMPTS} is reached. The per-IP half — which stops an
 * attacker from spreading the same password over many accounts — is
 * {@link LoginRateLimitFilter}.
 *
 * <p>Also the single place where authentication outcomes are audited; before
 * this, a failed login left no trace at all, so an attack in progress was
 * invisible.
 */
@Service
public class LoginAttemptService {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    /** Separate logger so the audit trail can be routed/retained on its own. */
    private static final Logger audit = LoggerFactory.getLogger("com.markly.audit");

    private final UserRepository userRepository;

    public LoginAttemptService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void onFailure(String username, String clientIp, String reason) {
        User user = userRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (user == null) {
            // Deliberately not created or tracked per-username: an unknown
            // username is already covered by the per-IP limit, and keeping
            // state for it would let anyone fill the table.
            audit.warn("LOGIN_FAILURE username='{}' ip={} reason=UNKNOWN_USER", username, clientIp);
            return;
        }

        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(Instant.now().plus(LOCK_DURATION));
            user.setFailedLoginAttempts(0);
            audit.warn("ACCOUNT_LOCKED username='{}' ip={} until={} after {} failed attempts",
                    user.getUsername(), clientIp, user.getLockedUntil(), MAX_FAILED_ATTEMPTS);
        } else {
            audit.warn("LOGIN_FAILURE username='{}' ip={} reason={} attempt={}/{}",
                    user.getUsername(), clientIp, reason, attempts, MAX_FAILED_ATTEMPTS);
        }
        userRepository.save(user);
    }

    @Transactional
    public void onSuccess(String username, String clientIp) {
        userRepository.findByUsernameIgnoreCase(username).ifPresent(user -> {
            audit.info("LOGIN_SUCCESS username='{}' ip={}", user.getUsername(), clientIp);
            if (user.getFailedLoginAttempts() != 0 || user.getLockedUntil() != null) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            }
        });
    }

    public void onBlocked(String username, String clientIp, String reason) {
        audit.warn("LOGIN_BLOCKED username='{}' ip={} reason={}", username, clientIp, reason);
    }
}
