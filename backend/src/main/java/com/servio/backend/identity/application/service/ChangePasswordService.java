package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.in.command.ChangePasswordCommand;
import com.servio.backend.identity.application.port.in.usecase.ChangePasswordUseCase;
import com.servio.backend.identity.application.port.out.UserRepositoryPort;
import com.servio.backend.identity.domain.User;
import com.servio.backend.notification.mail.application.port.in.SendEmailUseCase;
import com.servio.backend.notification.mail.domain.ContentType;
import com.servio.backend.notification.mail.domain.Email;
import com.servio.backend.shared.exception.InvalidArgumentException;
import com.servio.backend.shared.exception.UnauthorizedException;
import com.servio.backend.shared.mail.TemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final SendEmailUseCase sendEmailUseCase;
    private final TemplateRenderer templateRenderer;

    @Value("${mail.default-sender}")
    private String defaultSender;

    @Override
    public void changePassword(ChangePasswordCommand command, User user) {
        if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            throw new UnauthorizedException("La contraseña actual es incorrecta");
        }

        if (!command.newPassword().equals(command.confirmationPassword())) {
            throw new InvalidArgumentException("Las contraseñas no coinciden");
        }

        user.setPassword(passwordEncoder.encode(command.newPassword()));
        userRepositoryPort.save(user);
        enviarEmailConfirmacion(user);
    }

    private void enviarEmailConfirmacion(User user) {
        try {
            String html = templateRenderer.render("password_changed", Map.of("name", user.getFirstname()));
            sendEmailUseCase.send(
                    Email.builder()
                            .from(defaultSender)
                            .to(user.getEmail())
                            .subject("Tu contraseña fue cambiada")
                            .body(html)
                            .contentType(ContentType.HTML)
                            .build()
            );
        } catch (Exception e) {
            log.warn("No se pudo enviar email de confirmacion a {}: {}", user.getEmail(), e.getMessage());
        }
    }
}