package com.servio.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String resource) {
        super(resource + " no encontrado", HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(resource + " no encontrado con " + field + ": " + value, HttpStatus.NOT_FOUND);
    }
}