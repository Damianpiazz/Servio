package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.in.command.RegisterCommand;
import com.servio.backend.identity.application.port.in.usecase.RegisterUseCase;
import com.servio.backend.identity.application.port.out.TokenProviderPort;
import com.servio.backend.identity.application.port.out.UserRepositoryPort;
import com.servio.backend.identity.domain.Role;
import com.servio.backend.identity.domain.Token;
import com.servio.backend.identity.domain.User;
import com.servio.backend.identity.domain.exception.EmailAlreadyExistsException;
import com.servio.backend.notification.mail.application.port.in.SendEmailUseCase;
import com.servio.backend.notification.mail.domain.ContentType;
import com.servio.backend.notification.mail.domain.Email;
import com.servio.backend.notification.mail.infrastructure.template.TemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterService implements RegisterUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final TokenProviderPort tokenProviderPort;
    private final SendEmailUseCase sendEmailUseCase;
    private final TemplateRenderer templateRenderer;

    @Override
    public Token register(RegisterCommand command) {
        if (userRepositoryPort.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        User user = User.builder()
                .firstname(command.firstname())
                .lastname(command.lastname())
                .email(command.email())
                .password(passwordEncoder.encode(command.password()))
                .role(command.role() != null ? command.role() : Role.USER)
                .blocked(false)
                .build();

        User savedUser = userRepositoryPort.save(user);
        enviarEmailBienvenida(savedUser);

        return Token.builder()
                .accessToken(tokenProviderPort.generateAccessToken(savedUser.getEmail(), savedUser.getRole().name()))
                .refreshToken(tokenProviderPort.generateRefreshToken(savedUser.getEmail()))
                .build();
    }

    private void enviarEmailBienvenida(User user) {
        try {
            String html = templateRenderer.render("register", Map.of("name", user.getFirstname()));
            sendEmailUseCase.send(
                    Email.builder()
                            .from("no-reply@servio.com") // TODO: mover a properties
                            .to(user.getEmail())
                            .subject("Bienvenido a Servio")
                            .body(html)
                            .contentType(ContentType.HTML)
                            .build()
            );
        } catch (Exception e) {
            log.warn("No se pudo enviar email de bienvenida a {}: {}", user.getEmail(), e.getMessage());
        }
    }
}