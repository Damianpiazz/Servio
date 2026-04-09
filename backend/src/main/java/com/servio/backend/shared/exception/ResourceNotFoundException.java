package com.servio.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String resource) {
        super(resource + " not found", HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(resource + " not found with " + field + ": " + value, HttpStatus.NOT_FOUND);
    }
}