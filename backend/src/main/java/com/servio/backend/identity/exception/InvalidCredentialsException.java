package com.servio.backend.identity.exception;

import com.servio.backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AppException {
    public InvalidCredentialsException() {
        super("Invalid credentials", HttpStatus.UNAUTHORIZED);
    }
}