package com.servio.backend.mail.validation;

import com.servio.backend.mail.model.Email;
import com.servio.backend.shared.exception.InvalidArgumentException;
import org.springframework.stereotype.Component;

@Component
public class EmailValidator {

    public void validate(Email email) {
        if (email == null) {
            throw new InvalidArgumentException("Email cannot be null");
        }

        if (email.getFrom() == null || email.getFrom().isBlank()) {
            throw new InvalidArgumentException("Sender is required");
        }

        if (email.getTo() == null || email.getTo().isEmpty()) {
            throw new InvalidArgumentException("At least one recipient is required");
        }

        if (email.getTo().stream().anyMatch(e -> e == null || e.isBlank())) {
            throw new InvalidArgumentException("Recipients must be valid");
        }

        if (email.getSubject() == null || email.getSubject().isBlank()) {
            throw new InvalidArgumentException("Subject is required");
        }

        if (email.getTemplate() == null || email.getTemplate().isBlank()) {
            throw new InvalidArgumentException("Template is required");
        }
    }
}