package com.servio.backend.storage.validation;

import com.servio.backend.shared.exception.InvalidArgumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ImageValidator {

    @Value("${image.upload.max-size-bytes:2097152}")
    private long maxSizeBytes;

    @Value("${image.upload.allowed-types:image/jpeg,image/png,image/webp}")
    private List<String> allowedTypes;

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidArgumentException("File cannot be empty");
        }

        if (file.getSize() > maxSizeBytes) {
            throw new InvalidArgumentException(
                    "File exceeds maximum size of " + (maxSizeBytes / 1024 / 1024) + "MB"
            );
        }

        if (!allowedTypes.contains(file.getContentType())) {
            throw new InvalidArgumentException(
                    "Only the following types are allowed: " + String.join(", ", allowedTypes)
            );
        }
    }
}