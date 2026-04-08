package com.servio.backend.mail.service;

import com.servio.backend.mail.model.Email;
import com.servio.backend.mail.validation.EmailValidator;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Profile({"dev", "docker"})
@Service
@RequiredArgsConstructor
public class MailhogEmailService implements IEmailService {

    private final JavaMailSender mailSender;
    private final ITemplateService templateService;
    private final EmailValidator emailValidator;

    @Override
    public void send(Email email) {
        emailValidator.validate(email);

        try {
            String body = templateService.render(
                    email.getTemplate(),
                    email.getVariables()
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(email.getFrom());
            helper.setTo(email.getTo().toArray(new String[0]));
            helper.setSubject(email.getSubject());
            helper.setText(body, true);

            if (!email.getCc().isEmpty()) {
                helper.setCc(email.getCc().toArray(new String[0]));
            }

            if (!email.getBcc().isEmpty()) {
                helper.setBcc(email.getBcc().toArray(new String[0]));
            }

            mailSender.send(message);

            log.info("Email sent using template '{}' to {}",
                    email.getTemplate(), email.getTo());

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email.getTo(), e.getMessage(), e);
            throw new RuntimeException("Could not send email", e);
        }
    }
}