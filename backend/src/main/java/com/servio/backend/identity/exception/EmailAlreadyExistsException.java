package com.servio.backend.identity.exception;

import com.servio.backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends AppException {
    public EmailAlreadyExistsException(String email) {
        super("Email " + email + " is already registered", HttpStatus.CONFLICT);
    }
}