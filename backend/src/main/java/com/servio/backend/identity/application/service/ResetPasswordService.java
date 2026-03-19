package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.in.command.ResetPasswordCommand;
import com.servio.backend.identity.application.port.in.usecase.ResetPasswordUseCase;
import com.servio.backend.identity.application.port.out.PasswordResetTokenRepositoryPort;
import com.servio.backend.identity.application.port.out.UserRepositoryPort;
import com.servio.backend.identity.domain.User;
import com.servio.backend.identity.domain.exception.InvalidTokenException;
import com.servio.backend.identity.domain.exception.TokenExpiredException;
import com.servio.backend.notification.mail.application.port.in.SendEmailUseCase;
import com.servio.backend.notification.mail.domain.ContentType;
import com.servio.backend.notification.mail.domain.Email;
import com.servio.backend.notification.mail.infrastructure.template.TemplateRenderer;
import com.servio.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordResetTokenRepositoryPort tokenRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final SendEmailUseCase sendEmailUseCase;
    private final TemplateRenderer templateRenderer;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void requestReset(String email) {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));

        String token = UUID.randomUUID().toString();
        tokenRepositoryPort.save(token, user, LocalDateTime.now().plusMinutes(30));
        enviarEmailReset(user, token);
    }

    @Override
    public void reset(ResetPasswordCommand command) {
        PasswordResetTokenRepositoryPort.PasswordResetTokenData resetToken =
                tokenRepositoryPort.findByToken(command.token())
                        .orElseThrow(InvalidTokenException::new);

        if (resetToken.isExpired()) {
            throw new TokenExpiredException();
        }

        User user = resetToken.user();
        user.setPassword(passwordEncoder.encode(command.newPassword()));
        userRepositoryPort.save(user);
        tokenRepositoryPort.delete(command.token());
    }

    private void enviarEmailReset(User user, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            String html = templateRenderer.render("reset_password", Map.of(
                    "name", user.getFirstname(),
                    "resetUrl", resetUrl
            ));
            sendEmailUseCase.send(
                    Email.builder()
                            .from("no-reply@servio.com") // TODO: mover a properties
                            .to(user.getEmail())
                            .subject("Recuperación de contraseña")
                            .body(html)
                            .contentType(ContentType.HTML)
                            .build()
            );
        } catch (Exception e) {
            log.warn("No se pudo enviar email de reset a {}: {}", user.getEmail(), e.getMessage());
        }
    }
}