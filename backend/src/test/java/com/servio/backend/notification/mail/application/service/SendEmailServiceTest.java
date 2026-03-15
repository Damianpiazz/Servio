package com.servio.backend.notification.mail.application.service;

import com.servio.backend.notification.mail.application.port.out.EmailSenderPort;
import com.servio.backend.notification.mail.domain.ContentType;
import com.servio.backend.notification.mail.domain.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendEmailServiceTest {

    @Mock
    private EmailSenderPort emailSenderPort;

    @InjectMocks
    private SendEmailService sendEmailService;

    @Test
    void debeDelegarElEnvioAlPort() {
        Email email = buildEmail();

        sendEmailService.send(email);

        verify(emailSenderPort, times(1)).send(email);
    }

    @Test
    void debePasarElEmailSinModificaciones() {
        Email email = buildEmail();
        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);

        sendEmailService.send(email);

        verify(emailSenderPort).send(captor.capture());
        Email captured = captor.getValue();

        assertThat(captured.getFrom()).isEqualTo(email.getFrom());
        assertThat(captured.getTo()).isEqualTo(email.getTo());
        assertThat(captured.getSubject()).isEqualTo(email.getSubject());
        assertThat(captured.getBody()).isEqualTo(email.getBody());
    }

    @Test
    void debePropagarlExcepcionDelAdapter() {
        Email email = buildEmail();
        doThrow(new RuntimeException("SMTP caído"))
                .when(emailSenderPort).send(any());

        assertThatThrownBy(() -> sendEmailService.send(email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP caído");
    }

    private Email buildEmail() {
        return Email.builder()
                .from("no-reply@servio.com")
                .to("juan@test.com")
                .subject("Asunto")
                .body("<h1>Hola</h1>")
                .contentType(ContentType.HTML)
                .build();
    }
}