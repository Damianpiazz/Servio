package com.servio.backend.identity.domain.exception;

import com.servio.backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends AppException {
    public TokenExpiredException() {
        super("El token ha expirado", HttpStatus.UNAUTHORIZED);
    }
}