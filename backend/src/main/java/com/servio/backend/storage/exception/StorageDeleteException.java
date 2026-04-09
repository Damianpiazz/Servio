package com.servio.backend.storage.exception;

import org.springframework.http.HttpStatus;

public class StorageDeleteException extends StorageException {
    public StorageDeleteException(String path) {
        super("Failed to delete file: " + path, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}