package com.servio.backend.notification.mail.infrastructure.adapter;


import com.servio.backend.notification.mail.application.port.out.EmailSenderPort;
import com.servio.backend.notification.mail.domain.ContentType;
import com.servio.backend.notification.mail.domain.Email;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailhogEmailAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;

    @Override
    public void send(Email email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(email.getFrom());
            helper.setTo(email.getTo().toArray(new String[0]));
            helper.setSubject(email.getSubject());
            helper.setText(email.getBody(), email.getContentType() == ContentType.HTML);

            if (!email.getCc().isEmpty()) {
                helper.setCc(email.getCc().toArray(new String[0]));
            }
            if (!email.getBcc().isEmpty()) {
                helper.setBcc(email.getBcc().toArray(new String[0]));
            }

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Error al enviar email a {}: {}", email.getTo(), e.getMessage(), e);
            throw new RuntimeException("No se pudo enviar el correo", e);
        }
    }
}