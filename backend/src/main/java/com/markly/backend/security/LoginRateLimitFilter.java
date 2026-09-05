package com.markly.backend.security;

import com.markly.backend.exception.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Caps how many <em>failed</em> login attempts one client address may make per
 * window, so a
 * password-guessing run is stopped before the per-account lockout in
 * {@link LoginAttemptService} even comes into play (and, unlike that lockout,
 * this also covers an attacker spraying one password across many accounts).
 *
 * <p>The counters are in-memory, which is enough for the single instance this
 * app is deployed as; a multi-instance deployment would need them in Redis or
 * at the edge, since each instance would otherwise allow the full quota.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    public static final int MAX_ATTEMPTS_PER_WINDOW = 15;
    static final Duration WINDOW = Duration.ofMinutes(5);
    /** Guards against the map growing without bound under a distributed attack. */
    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private final Map<String, Window> attempts = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final LoginAttemptService loginAttemptService;
    private final ClientIpResolver clientIpResolver;

    public LoginRateLimitFilter(
            ObjectMapper objectMapper,
            LoginAttemptService loginAttemptService,
            ClientIpResolver clientIpResolver) {
        this.objectMapper = objectMapper;
        this.loginAttemptService = loginAttemptService;
        this.clientIpResolver = clientIpResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !(HttpMethod.POST.matches(request.getMethod())
                && request.getRequestURI().endsWith("/api/auth/login"));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String clientIp = clientIpResolver.resolve(request);
        // The slot is taken before the attempt runs, not after it answers:
        // counting afterwards let a burst of concurrent requests all pass the
        // check while the count still read 14, overshooting the quota. A
        // successful login gives its slot back below.
        Window window = reserveSlot(clientIp);
        if (window == null) {
            loginAttemptService.onBlocked("-", clientIp, "RATE_LIMIT");
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(WINDOW.toSeconds()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(),
                    new ApiError("Твърде много опити за вход. Опитайте отново след няколко минути."));
            return;
        }

        boolean succeeded = false;
        try {
            filterChain.doFilter(request, response);
            succeeded = response.getStatus() == HttpStatus.OK.value();
        } finally {
            // Only failures count against the quota. A whole computer lab
            // shares one NAT address, and charging their successful logins
            // would lock the room out during ordinary use; a guessing run, by
            // definition, produces failures. A request that blew up on the way
            // out keeps its slot — it never proved itself a success, and the
            // release has to happen here rather than after doFilter so an
            // exception cannot skip the accounting entirely.
            if (succeeded) {
                window.count.decrementAndGet();
            }
        }
    }

    /**
     * @return the window the attempt was charged to, or {@code null} if the
     * client has used up its quota for the current one.
     */
    private Window reserveSlot(String clientIp) {
        Instant now = Instant.now();
        if (attempts.size() > MAX_TRACKED_CLIENTS) {
            attempts.values().removeIf(expired -> expired.hasExpired(now));
        }
        Window window = attempts.compute(clientIp, (ip, existing) ->
                existing == null || existing.hasExpired(now) ? new Window(now) : existing);
        return window.count.incrementAndGet() <= MAX_ATTEMPTS_PER_WINDOW ? window : null;
    }

    private static final class Window {
        private final Instant startedAt;
        private final AtomicInteger count = new AtomicInteger();

        private Window(Instant startedAt) {
            this.startedAt = startedAt;
        }

        private boolean hasExpired(Instant now) {
            return startedAt.plus(WINDOW).isBefore(now);
        }
    }
}
