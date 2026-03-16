package com.servio.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AppException {

    public UnauthorizedException() {
        super("No autenticado", HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}