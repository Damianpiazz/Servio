package com.servio.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class InvalidArgumentException extends AppException {

    public InvalidArgumentException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}