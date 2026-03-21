package com.servio.backend.notification.mail;

import com.servio.backend.BaseIntegrationTest;
import com.servio.backend.notification.mail.application.port.in.SendEmailUseCase;
import com.servio.backend.notification.mail.domain.ContentType;
import com.servio.backend.notification.mail.domain.Email;
import com.servio.backend.shared.mail.TemplateRenderer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

class SendEmailIntegrationTest extends BaseIntegrationTest {

    @Autowired private SendEmailUseCase sendEmailUseCase;
    @Autowired private TemplateRenderer templateRenderer;

    @Test
    void debeEnviarEmailDeRegistroAMailhog() {
        String html = templateRenderer.render("test", Map.of(
                "name", "Juan",
                "orderId", "ORD-123",
                "total", 4500.00
        ));

        sendEmailUseCase.send(
                Email.builder()
                        .from("no-reply@servio.com")
                        .to("juan@test.com")
                        .subject("Test de email")
                        .body(html)
                        .contentType(ContentType.HTML)
                        .build()
        );
    }
}