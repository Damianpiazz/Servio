package com.servio.backend.storage.service;

import com.servio.backend.storage.model.UploadedFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IImageService {

    UploadedFile upload(MultipartFile file, String folder);

    List<UploadedFile> uploadMultiple(List<MultipartFile> files, String folder);

    void delete(String fullPath);

    void deleteMultiple(List<String> fullPaths);
}