package com.servio.backend.notification.mail;

import com.servio.backend.notification.mail.application.port.in.SendEmailUseCase;
import com.servio.backend.notification.mail.domain.ContentType;
import com.servio.backend.notification.mail.domain.Email;
import com.servio.backend.notification.mail.infrastructure.template.TemplateRenderer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
class SendEmailIntegrationTest {

    @Autowired
    private SendEmailUseCase sendEmailUseCase;

    @Autowired
    private TemplateRenderer templateRenderer;

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