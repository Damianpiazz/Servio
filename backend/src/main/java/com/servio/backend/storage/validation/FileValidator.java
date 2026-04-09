package com.servio.backend.storage.validation;

import com.servio.backend.shared.exception.InvalidArgumentException;
import com.servio.backend.storage.model.FileToUpload;
import org.springframework.stereotype.Component;

@Component
public class FileValidator {

    public void validate(FileToUpload file) {
        if (file == null) {
            throw new InvalidArgumentException("File cannot be null");
        }

        if (file.getFileName() == null || file.getFileName().isBlank()) {
            throw new InvalidArgumentException("File name is required");
        }

        if (file.getContent() == null || file.getContent().length == 0) {
            throw new InvalidArgumentException("File content is required");
        }

        if (file.getContentType() == null || file.getContentType().isBlank()) {
            throw new InvalidArgumentException("Content type is required");
        }
    }
}