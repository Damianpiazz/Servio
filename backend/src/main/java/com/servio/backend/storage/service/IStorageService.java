package com.servio.backend.storage.service;

import com.servio.backend.storage.model.FileToUpload;
import com.servio.backend.storage.model.UploadedFile;

import java.io.InputStream;
import java.util.List;

public interface IStorageService {
    UploadedFile upload(FileToUpload file);
    List<UploadedFile> uploadMultiple(List<FileToUpload> files);
    InputStream download(String fullPath);
    void delete(String fullPath);
    void deleteMultiple(List<String> fullPaths);
    String getUrl(String fullPath);
    boolean exists(String fullPath);
}