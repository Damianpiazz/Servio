package com.servio.backend.storage.service;

import com.servio.backend.storage.model.FileToUpload;
import com.servio.backend.storage.model.UploadedFile;
import com.servio.backend.storage.validation.ImageValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService implements IImageService {

    private final IStorageService storageService;
    private final ImageValidator imageValidator;

    @Override
    public UploadedFile upload(MultipartFile file, String folder) {
        imageValidator.validate(file);

        try {
            FileToUpload fileToUpload = FileToUpload.of(
                    UUID.randomUUID() + "_" + file.getOriginalFilename(),
                    file.getBytes(),
                    file.getContentType(),
                    file.getSize(),
                    folder
            );

            return storageService.upload(fileToUpload);

        } catch (IOException e) {
            throw new RuntimeException("Could not process file", e);
        }
    }

    @Override
    public List<UploadedFile> uploadMultiple(List<MultipartFile> files, String folder) {
        return files.stream()
                .map(file -> upload(file, folder))
                .toList();
    }

    @Override
    public void delete(String fullPath) {
        storageService.delete(fullPath);
    }

    @Override
    public void deleteMultiple(List<String> fullPaths) {
        storageService.deleteMultiple(fullPaths);
    }
}