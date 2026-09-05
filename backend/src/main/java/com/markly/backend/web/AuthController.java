package com.markly.backend.web;

import com.markly.backend.repository.UserRepository;
import com.markly.backend.security.AppUserPrincipal;
import com.markly.backend.security.AuthCookieService;
import com.markly.backend.security.ClientIpResolver;
import com.markly.backend.security.JwtService;
import com.markly.backend.security.LoginAttemptService;
import com.markly.backend.service.UserValidationService;
import com.markly.backend.web.dto.ChangePasswordRequest;
import com.markly.backend.web.dto.LoginRequest;
import com.markly.backend.web.dto.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthCookieService authCookieService;
    private final LoginAttemptService loginAttemptService;
    private final UserRepository userRepository;
    private final UserValidationService userValidationService;
    private final PasswordEncoder passwordEncoder;
    private final ClientIpResolver clientIpResolver;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AuthCookieService authCookieService,
            LoginAttemptService loginAttemptService,
            UserRepository userRepository,
            UserValidationService userValidationService,
            PasswordEncoder passwordEncoder,
            ClientIpResolver clientIpResolver) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authCookieService = authCookieService;
        this.loginAttemptService = loginAttemptService;
        this.userRepository = userRepository;
        this.userValidationService = userValidationService;
        this.passwordEncoder = passwordEncoder;
        this.clientIpResolver = clientIpResolver;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = clientIpResolver.resolve(httpRequest);
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            var principal = (AppUserPrincipal) authentication.getPrincipal();
            loginAttemptService.onSuccess(principal.getUsername(), clientIp);

            String role = principal.getUser().getRole().name();
            String token = jwtService.issueToken(
                    principal.getUsername(), role, principal.getUser().getTokenVersion());
            String csrfToken = jwtService.csrfTokenFor(jwtService.parseClaims(token));

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, authCookieService.create(token).toString())
                    .body(new LoginResponse(principal.getUsername(), role, csrfToken));
        } catch (LockedException ex) {
            loginAttemptService.onBlocked(request.username(), clientIp, "ACCOUNT_LOCKED");
            throw new ResponseStatusException(HttpStatus.LOCKED,
                    "Акаунтът е временно заключен заради твърде много неуспешни опити. Опитайте по-късно.");
        } catch (DisabledException ex) {
            loginAttemptService.onBlocked(request.username(), clientIp, "ACCOUNT_DISABLED");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Акаунтът е деактивиран. Свържете се с администратор.");
        } catch (AuthenticationException ex) {
            loginAttemptService.onFailure(request.username(), clientIp, "BAD_CREDENTIALS");
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    /**
     * The SPA cannot read the httpOnly cookie, so on a page reload this is how
     * it finds out whether the session is still alive, who it belongs to, and
     * what CSRF token to send.
     */
    @GetMapping("/me")
    public LoginResponse me(@AuthenticationPrincipal AppUserPrincipal principal, HttpServletRequest request) {
        String csrfToken = authCookieService.readToken(request)
                .map(token -> jwtService.csrfTokenFor(jwtService.parseClaims(token)))
                .orElseThrow(() -> new BadCredentialsException("No session"));
        return new LoginResponse(
                principal.getUsername(), principal.getUser().getRole().name(), csrfToken);
    }

    /**
     * Until this existed the password policy only applied at account creation:
     * nobody could rotate a password, including the seeded admin's, without an
     * admin creating a whole new account.
     *
     * <p>The current password is required, so someone sitting at an unlocked
     * browser cannot take the account over. Every other session is dropped
     * (the token version moves), and this one is re-issued a fresh cookie so
     * the user is not logged out of the tab they just used.
     */
    @PostMapping("/password")
    public ResponseEntity<LoginResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal,
            HttpServletRequest httpRequest) {
        var user = userRepository.findByUsernameIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new BadCredentialsException("No such user"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            loginAttemptService.onBlocked(
                    user.getUsername(), clientIpResolver.resolve(httpRequest), "PASSWORD_CHANGE_WRONG_CURRENT");
            // Deliberately a 400 and not a 401: the SPA treats a 401 as "the
            // session is gone" and logs out, which would be a surprising way
            // to answer a typo in the current-password field.
            throw new IllegalArgumentException("Текущата парола е грешна");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Новата парола трябва да е различна от текущата");
        }
        userValidationService.validatePassword(user.getUsername(), request.newPassword());

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        String role = user.getRole().name();
        String token = jwtService.issueToken(user.getUsername(), role, user.getTokenVersion());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieService.create(token).toString())
                .body(new LoginResponse(user.getUsername(), role,
                        jwtService.csrfTokenFor(jwtService.parseClaims(token))));
    }

    /**
     * Clearing the cookie alone would leave the token usable by anyone who had
     * copied it, so the user's token version is bumped as well — that retires
     * every token issued to this account, on every device.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AppUserPrincipal principal) {
        userRepository.findByUsernameIgnoreCase(principal.getUsername()).ifPresent(user -> {
            user.setTokenVersion(user.getTokenVersion() + 1);
            userRepository.save(user);
        });
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookieService.clear().toString())
                .build();
    }
}
