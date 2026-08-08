package com.shkolo.backend.web;

import com.shkolo.backend.security.AppUserPrincipal;
import com.shkolo.backend.security.JwtService;
import com.shkolo.backend.web.dto.LoginRequest;
import com.shkolo.backend.web.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            var principal = (AppUserPrincipal) authentication.getPrincipal();
            String role = principal.getUser().getRole().name();
            String token = jwtService.issueToken(principal.getUsername(), role);
            return new LoginResponse(token, principal.getUsername(), role);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }
}
