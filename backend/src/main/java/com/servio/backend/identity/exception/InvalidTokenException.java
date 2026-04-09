package com.servio.backend.identity.exception;

import com.servio.backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends AppException {
    public InvalidTokenException() {
        super("Invalid token", HttpStatus.UNAUTHORIZED);
    }
}