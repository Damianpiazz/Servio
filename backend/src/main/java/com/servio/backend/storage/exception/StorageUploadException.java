package com.servio.backend.storage.exception;

import org.springframework.http.HttpStatus;

public class StorageUploadException extends StorageException {
    public StorageUploadException(String path) {
        super("Failed to upload file: " + path, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}