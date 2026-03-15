package com.servio.backend.storage.infrastructure.adapter;

import com.servio.backend.storage.application.port.out.StoragePort;
import com.servio.backend.storage.domain.StorageFile;
import com.servio.backend.storage.domain.StorageObject;
import com.servio.backend.storage.infrastructure.config.ObjectStorageConfig;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioStorageAdapter implements StoragePort {

    private final MinioClient minioClient;
    private final ObjectStorageConfig objectStorageConfig;

    @Override
    public StorageObject upload(StorageFile file) {
        try {
            ensureBucketExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(objectStorageConfig.getBucket())
                            .object(file.getFullPath())
                            .stream(new ByteArrayInputStream(file.getContent()), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String url = getUrl(file.getFullPath());

            return StorageObject.builder()
                    .fileName(file.getFileName())
                    .fullPath(file.getFullPath())
                    .url(url)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .build();

        } catch (Exception e) {
            log.error("Error al subir archivo {}: {}", file.getFullPath(), e.getMessage());
            throw new RuntimeException("No se pudo subir el archivo", e);
        }
    }

    @Override
    public InputStream download(String fullPath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(objectStorageConfig.getBucket())
                            .object(fullPath)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error al descargar archivo {}: {}", fullPath, e.getMessage());
            throw new RuntimeException("No se pudo descargar el archivo", e);
        }
    }

    @Override
    public void delete(String fullPath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(objectStorageConfig.getBucket())
                            .object(fullPath)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error al eliminar archivo {}: {}", fullPath, e.getMessage());
            throw new RuntimeException("No se pudo eliminar el archivo", e);
        }
    }

    @Override
    public String getUrl(String fullPath) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(objectStorageConfig.getBucket())
                            .object(fullPath)
                            .method(Method.GET)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error al obtener URL de {}: {}", fullPath, e.getMessage());
            throw new RuntimeException("No se pudo obtener la URL del archivo", e);
        }
    }

    @Override
    public boolean exists(String fullPath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(objectStorageConfig.getBucket())
                            .object(fullPath)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            log.error("Error al verificar existencia de {}: {}", fullPath, e.getMessage());
            throw new RuntimeException("No se pudo verificar el archivo", e);
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(objectStorageConfig.getBucket())
                        .build()
        );
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(objectStorageConfig.getBucket())
                            .build()
            );
            log.info("Bucket creado: {}", objectStorageConfig.getBucket());
        }
    }
}