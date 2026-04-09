package com.servio.backend.storage.exception;

import org.springframework.http.HttpStatus;

public class StorageDownloadException extends StorageException {
    public StorageDownloadException(String path) {
        super("Failed to download file: " + path, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}