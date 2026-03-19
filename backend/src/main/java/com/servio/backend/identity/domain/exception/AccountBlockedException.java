package com.servio.backend.identity.domain.exception;

import com.servio.backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class AccountBlockedException extends AppException {
    public AccountBlockedException() {
        super("La cuenta está bloqueada. Contactá al soporte.", HttpStatus.FORBIDDEN);
    }
}