package com.servio.backend.mail.service;

import com.servio.backend.mail.model.Email;
import com.servio.backend.mail.validation.EmailValidator;
import com.servio.backend.shared.exception.InvalidArgumentException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailhogEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ITemplateService templateService;

    @Mock
    private EmailValidator emailValidator;

    @Mock
    private MimeMessage mimeMessage;

    private MailhogEmailService service;

    @BeforeEach
    void setUp() {
        service = new MailhogEmailService(mailSender, templateService, emailValidator);
    }

    // ─── happy path ───────────────────────────────────────────────

    @Test
    void send_shouldSendEmail_whenInputIsValid() throws Exception {
        Email email = validEmail();
        when(templateService.render(eq("register"), any())).thenReturn("<h1>Welcome</h1>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        // MimeMessageHelper writes directly onto MimeMessage — we just verify send() is called
        doNothing().when(mailSender).send(mimeMessage);

        assertThatNoException().isThrownBy(() -> service.send(email));

        verify(emailValidator).validate(email);
        verify(templateService).render(eq("register"), any());
        verify(mailSender).send(mimeMessage);
    }

    // ─── validator is always called ───────────────────────────────

    @Test
    void send_shouldDelegateValidationToValidator() {
        Email email = validEmail();
        doThrow(new InvalidArgumentException("Sender is required"))
                .when(emailValidator).validate(email);

        assertThatThrownBy(() -> service.send(email))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Sender");

        verify(emailValidator).validate(email);
        verifyNoInteractions(templateService, mailSender);
    }

    // ─── template rendering failure ───────────────────────────────

    @Test
    void send_shouldThrowRuntimeException_whenTemplateFails() {
        Email email = validEmail();
        when(templateService.render(any(), any())).thenThrow(new RuntimeException("Template error"));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertThatThrownBy(() -> service.send(email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Could not send email");
    }

    // ─── cc / bcc ─────────────────────────────────────────────────

    @Test
    void send_shouldSendWithCcAndBcc() {
        Email email = Email.builder()
                .from("from@servio.com")
                .to(List.of("to@example.com"))
                .subject("Subject")
                .template("register")
                .variables(Map.of())
                .cc(List.of("cc@example.com"))
                .bcc(List.of("bcc@example.com"))
                .build();

        when(templateService.render(any(), any())).thenReturn("<p>body</p>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(mimeMessage);

        assertThatNoException().isThrownBy(() -> service.send(email));
        verify(mailSender).send(mimeMessage);
    }

    // ─── helper ───────────────────────────────────────────────────

    private Email validEmail() {
        return Email.builder()
                .from("from@servio.com")
                .to(List.of("user@example.com"))
                .subject("Welcome")
                .template("register")
                .variables(Map.of("name", "John"))
                .build();
    }
}