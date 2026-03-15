package com.servio.backend.notification.mail.application.service;

import com.servio.backend.notification.mail.application.port.in.SendEmailUseCase;
import com.servio.backend.notification.mail.application.port.out.EmailSenderPort;
import com.servio.backend.notification.mail.domain.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendEmailService implements SendEmailUseCase {

    private final EmailSenderPort emailSenderPort;

    @Override
    public void send(Email email) {
        log.info("Enviando email a {} con asunto '{}'", email.getTo(), email.getSubject());
        emailSenderPort.send(email);
        log.info("Email enviado correctamente a {}", email.getTo());
    }
}