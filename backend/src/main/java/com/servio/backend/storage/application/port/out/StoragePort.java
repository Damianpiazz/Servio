package com.servio.backend.storage.application.port.out;

import com.servio.backend.storage.domain.StorageFile;
import com.servio.backend.storage.domain.StorageObject;

import java.io.InputStream;

public interface StoragePort {
    StorageObject upload(StorageFile file);
    InputStream download(String fullPath);
    void delete(String fullPath);
    String getUrl(String fullPath);
    boolean exists(String fullPath);
}