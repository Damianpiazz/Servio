package com.servio.backend.mail.validation;

import com.servio.backend.mail.model.Email;
import com.servio.backend.shared.exception.InvalidArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class EmailValidatorTest {

    private EmailValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EmailValidator();
    }

    // ─── happy path ───────────────────────────────────────────────

    @Test
    void validate_shouldPass_whenEmailIsValid() {
        Email email = Email.builder()
                .from("sender@servio.com")
                .to(List.of("recipient@example.com"))
                .subject("Hello")
                .template("register")
                .variables(java.util.Map.of())
                .build();

        assertThatNoException().isThrownBy(() -> validator.validate(email));
    }

    // ─── null email ───────────────────────────────────────────────

    @Test
    void validate_shouldThrow_whenEmailIsNull() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("null");
    }

    // ─── from ─────────────────────────────────────────────────────

    @Test
    void validate_shouldThrow_whenFromIsNull() {
        Email email = buildEmail(null, List.of("to@example.com"), "Subject", "template");
        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Sender");
    }

    @Test
    void validate_shouldThrow_whenFromIsBlank() {
        Email email = buildEmail("   ", List.of("to@example.com"), "Subject", "template");
        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Sender");
    }

    // ─── to ───────────────────────────────────────────────────────

    @Test
    void validate_shouldThrow_whenToIsNull() {
        Email email = buildEmail("from@servio.com", null, "Subject", "template");
        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("recipient");
    }

    @Test
    void validate_shouldThrow_whenToIsEmpty() {
        Email email = buildEmail("from@servio.com", List.of(), "Subject", "template");
        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("recipient");
    }

    @Test
    void validate_shouldThrow_whenAnyRecipientIsBlank() {
        Email email = buildEmail("from@servio.com", List.of("ok@example.com", ""), "Subject", "template");
        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("valid");
    }

    // ─── subject ──────────────────────────────────────────────────

    @Test
    void validate_shouldThrow_whenSubjectIsNull() {
        Email email = buildEmail("from@servio.com", List.of("to@example.com"), null, "template");
        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Subject");
    }

    @Test
    void validate_shouldThrow_whenSubjectIsBlank() {
        Email email = buildEmail("from@servio.com", List.of("to@example.com"), "  ", "template");
        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Subject");
    }

    // ─── template ─────────────────────────────────────────────────

    @Test
    void validate_shouldThrow_whenTemplateIsNull() {
        Email email = buildEmail("from@servio.com", List.of("to@example.com"), "Subject", null);
        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Template");
    }

    @Test
    void validate_shouldThrow_whenTemplateIsBlank() {
        Email email = buildEmail("from@servio.com", List.of("to@example.com"), "Subject", "  ");
        assertThatThrownBy(() -> validator.validate(email))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Template");
    }

    // ─── helper ───────────────────────────────────────────────────

    private Email buildEmail(String from, List<String> to, String subject, String template) {
        return Email.builder()
                .from(from)
                .to(to)
                .subject(subject)
                .template(template)
                .variables(java.util.Map.of())
                .build();
    }
}