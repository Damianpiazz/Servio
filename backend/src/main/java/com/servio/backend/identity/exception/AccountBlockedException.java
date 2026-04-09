package com.servio.backend.identity.exception;

import com.servio.backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class AccountBlockedException extends AppException {
    public AccountBlockedException() {
        super("Account is blocked. Please contact support.", HttpStatus.FORBIDDEN);
    }
}