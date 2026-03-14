package com.servio.backend.notification.mail.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class EmailTest {

    @Test
    void debeConstruirseCorrectamente() {
        Email email = Email.builder()
                .from("no-reply@servio.com")
                .to("juan@test.com")
                .subject("Asunto")
                .body("Cuerpo")
                .contentType(ContentType.HTML)
                .cc(List.of("cc@test.com"))
                .bcc(List.of("bcc@test.com"))
                .build();

        assertThat(email.getFrom()).isEqualTo("no-reply@servio.com");
        assertThat(email.getTo()).containsExactly("juan@test.com");
        assertThat(email.getCc()).containsExactly("cc@test.com");
        assertThat(email.getBcc()).containsExactly("bcc@test.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void debeFallarSiFromEsVacioONulo(String from) {
        assertThatThrownBy(() -> Email.builder()
                .from(from)
                .to("juan@test.com")
                .subject("Asunto")
                .body("Cuerpo")
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("remitente");
    }

    @Test
    void debeFallarSiNoHayDestinatarios() {
        assertThatThrownBy(() -> Email.builder()
                .from("no-reply@servio.com")
                .subject("Asunto")
                .body("Cuerpo")
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("destinatario");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void debeFallarSiSubjectEsVacioONulo(String subject) {
        assertThatThrownBy(() -> Email.builder()
                .from("no-reply@servio.com")
                .to("juan@test.com")
                .subject(subject)
                .body("Cuerpo")
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asunto");
    }

    @Test
    void ccYBccSonOpcionalesYDefauleanAListaVacia() {
        Email email = Email.builder()
                .from("no-reply@servio.com")
                .to("juan@test.com")
                .subject("Asunto")
                .body("Cuerpo")
                .build();

        assertThat(email.getCc()).isEmpty();
        assertThat(email.getBcc()).isEmpty();
    }

    @Test
    void contentTypeDefaultEsText() {
        Email email = Email.builder()
                .from("no-reply@servio.com")
                .to("juan@test.com")
                .subject("Asunto")
                .body("Cuerpo")
                .build();

        assertThat(email.getContentType()).isEqualTo(ContentType.TEXT);
    }
}