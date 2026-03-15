package com.servio.backend.storage.application.port.in;

import com.servio.backend.storage.domain.StorageFile;
import com.servio.backend.storage.domain.StorageObject;

import java.io.InputStream;
import java.util.List;

public interface StorageUseCase {
    StorageObject upload(StorageFile file);
    List<StorageObject> uploadMultiple(List<StorageFile> files);
    InputStream download(String fullPath);
    void delete(String fullPath);
    void deleteMultiple(List<String> fullPaths);
    String getUrl(String fullPath);
    boolean exists(String fullPath);
}