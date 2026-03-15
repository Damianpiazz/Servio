package com.servio.backend.storage.application.service;

import com.servio.backend.storage.application.port.in.StorageUseCase;
import com.servio.backend.storage.application.port.out.StoragePort;
import com.servio.backend.storage.domain.StorageFile;
import com.servio.backend.storage.domain.StorageObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService implements StorageUseCase {

    private final StoragePort storagePort;

    @Override
    public StorageObject upload(StorageFile file) {
        log.info("Subiendo archivo: {}", file.getFullPath());
        StorageObject result = storagePort.upload(file);
        log.info("Archivo subido correctamente: {}", result.getUrl());
        return result;
    }

    @Override
    public List<StorageObject> uploadMultiple(List<StorageFile> files) {
        log.info("Subiendo {} archivos", files.size());
        return files.stream()
                .map(this::upload)
                .toList();
    }

    @Override
    public InputStream download(String fullPath) {
        log.info("Descargando archivo: {}", fullPath);
        return storagePort.download(fullPath);
    }

    @Override
    public void delete(String fullPath) {
        log.info("Eliminando archivo: {}", fullPath);
        storagePort.delete(fullPath);
        log.info("Archivo eliminado: {}", fullPath);
    }

    @Override
    public void deleteMultiple(List<String> fullPaths) {
        log.info("Eliminando {} archivos", fullPaths.size());
        fullPaths.forEach(this::delete);
    }

    @Override
    public String getUrl(String fullPath) {
        return storagePort.getUrl(fullPath);
    }

    @Override
    public boolean exists(String fullPath) {
        return storagePort.exists(fullPath);
    }
}