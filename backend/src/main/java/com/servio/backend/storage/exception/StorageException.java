package com.servio.backend.storage.exception;

import com.servio.backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public abstract class StorageException extends AppException {
    protected StorageException(String message, HttpStatus status) {
        super(message, status);
    }
}