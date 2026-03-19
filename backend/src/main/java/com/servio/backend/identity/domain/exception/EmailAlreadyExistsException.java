package com.servio.backend.identity.domain.exception;

import com.servio.backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends AppException {
    public EmailAlreadyExistsException(String email) {
        super("El email " + email + " ya está registrado", HttpStatus.CONFLICT);
    }
}