package com.servio.backend.notification.mail.infrastructure.adapter;

import com.servio.backend.notification.mail.domain.ContentType;
import com.servio.backend.notification.mail.domain.Email;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailhogEmailAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailhogEmailAdapter adapter;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() throws Exception {
        // MimeMessage real necesita una session, usamos la implementación de JavaMail
        mimeMessage = new MimeMessage((jakarta.mail.Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void debeEnviarMimeMessageCorrectamente() {
        Email email = Email.builder()
                .from("no-reply@servio.com")
                .to("juan@test.com")
                .subject("Asunto de prueba")
                .body("<h1>Hola</h1>")
                .contentType(ContentType.HTML)
                .build();

        adapter.send(email);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void debeEnviarConMultiplesDestinatarios() {
        Email email = Email.builder()
                .from("no-reply@servio.com")
                .to(List.of("a@test.com", "b@test.com", "c@test.com"))
                .subject("Asunto")
                .body("Cuerpo")
                .contentType(ContentType.TEXT)
                .build();

        adapter.send(email);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void debeLanzarExcepcionSiFallaSMTP() {
        Email email = Email.builder()
                .from("no-reply@servio.com")
                .to("juan@test.com")
                .subject("Asunto")
                .body("Cuerpo")
                .contentType(ContentType.TEXT)
                .build();

        doThrow(new RuntimeException("SMTP caído"))
                .when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> adapter.send(email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo enviar el correo");
    }
}