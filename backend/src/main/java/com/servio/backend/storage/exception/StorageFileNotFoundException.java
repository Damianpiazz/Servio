package com.servio.backend.storage.exception;

import org.springframework.http.HttpStatus;

public class StorageFileNotFoundException extends StorageException {
    public StorageFileNotFoundException(String path) {
        super("File not found: " + path, HttpStatus.NOT_FOUND);
    }
}