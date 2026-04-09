package com.servio.backend.identity.exception;

import com.servio.backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends AppException {
    public TokenExpiredException() {
        super("Token has expired", HttpStatus.UNAUTHORIZED);
    }
}