package com.servio.backend.shared.image;

import com.servio.backend.shared.exception.InvalidArgumentException;
import com.servio.backend.storage.application.port.in.StorageUseCase;
import com.servio.backend.storage.domain.StorageFile;
import com.servio.backend.storage.domain.StorageObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final StorageUseCase storageUseCase;

    @Value("${image.upload.max-size-bytes:2097152}")
    private long maxSizeBytes;

    @Value("${image.upload.allowed-types:image/jpeg,image/png,image/webp}")
    private List<String> allowedTypes;

    public StorageObject upload(MultipartFile file, String folder) {
        validar(file);
        try {
            return storageUseCase.upload(buildStorageFile(file, folder));
        } catch (IOException e) {
            throw new InvalidArgumentException("No se pudo procesar el archivo");
        }
    }

    public List<StorageObject> uploadMultiple(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            throw new InvalidArgumentException("Debe enviar al menos una imagen");
        }
        return files.stream()
                .map(file -> upload(file, folder))
                .toList();
    }

    public void delete(String fullPath) {
        storageUseCase.delete(fullPath);
    }

    public void deleteMultiple(List<String> fullPaths) {
        storageUseCase.deleteMultiple(fullPaths);
    }

    private StorageFile buildStorageFile(MultipartFile file, String folder) throws IOException {
        return StorageFile.builder()
                .fileName(UUID.randomUUID() + "_" + file.getOriginalFilename())
                .content(file.getBytes())
                .contentType(file.getContentType())
                .size(file.getSize())
                .folder(folder)
                .build();
    }

    private void validar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidArgumentException("El archivo no puede estar vacío");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidArgumentException(
                    "El archivo supera el tamaño máximo de " + (maxSizeBytes / 1024 / 1024) + "MB"
            );
        }
        if (!allowedTypes.contains(file.getContentType())) {
            throw new InvalidArgumentException(
                    "Solo se permiten imágenes de tipo: " + String.join(", ", allowedTypes)
            );
        }
    }
}