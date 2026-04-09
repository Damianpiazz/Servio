package com.servio.backend.identity.service.auth;

import com.servio.backend.identity.dto.request.RegisterRequest;
import com.servio.backend.identity.dto.response.AuthResponse;
import com.servio.backend.identity.exception.EmailAlreadyExistsException;
import com.servio.backend.identity.model.Role;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import com.servio.backend.identity.service.jwt.JwtService;
import com.servio.backend.mail.model.Email;
import com.servio.backend.mail.service.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final IEmailService emailService;

    @Value("${mail.default-sender}")
    private String defaultSender;

    public AuthResponse register(RegisterRequest request) {
        validateEmailUniqueness(request.getEmail());

        User user = buildUser(request);

        userRepository.save(user);

        sendWelcomeEmail(user);

        log.info("User registered: {}", user.getEmail());

        return new AuthResponse(
                jwtService.generateAccessToken(user.getEmail(), user.getRole().name(), user.getTokenVersion()),
                jwtService.generateRefreshToken(user.getEmail(), user.getTokenVersion())
        );
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    private User buildUser(RegisterRequest request) {
        return User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .blocked(false)
                .build();
    }

    private void sendWelcomeEmail(User user) {
        try {
            emailService.send(Email.builder()
                    .from(defaultSender)
                    .to(Collections.singletonList(user.getEmail()))
                    .subject("Welcome to Servio")
                    .template("register")
                    .variables(Map.of("name", user.getFirstname()))
                    .build());
        } catch (Exception e) {
            log.warn("Could not send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
}