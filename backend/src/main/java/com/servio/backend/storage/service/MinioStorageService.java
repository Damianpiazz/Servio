package com.servio.backend.storage.service;

import com.servio.backend.storage.config.StorageProperties;
import com.servio.backend.storage.exception.StorageDeleteException;
import com.servio.backend.storage.exception.StorageDownloadException;
import com.servio.backend.storage.exception.StorageUploadException;
import com.servio.backend.storage.model.FileToUpload;
import com.servio.backend.storage.model.UploadedFile;
import com.servio.backend.storage.validation.FileValidator;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService implements IStorageService {

    private final MinioClient minioClient;
    private final StorageProperties properties;
    private final FileValidator fileValidator;

    @Override
    public UploadedFile upload(FileToUpload file) {
        fileValidator.validate(file);

        try {
            ensureBucketExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.bucket())
                            .object(file.getFullPath())
                            .stream(new ByteArrayInputStream(file.getContent()), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return UploadedFile.builder()
                    .fileName(file.getFileName())
                    .fullPath(file.getFullPath())
                    .url(getUrl(file.getFullPath()))
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload file {}: {}", file.getFullPath(), e.getMessage(), e);
            throw new StorageUploadException(file.getFullPath());
        }
    }

    @Override
    public List<UploadedFile> uploadMultiple(List<FileToUpload> files) {
        return files.stream().map(this::upload).toList();
    }

    @Override
    public InputStream download(String fullPath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(properties.bucket())
                            .object(fullPath)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageDownloadException(fullPath);
        }
    }

    @Override
    public void delete(String fullPath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.bucket())
                            .object(fullPath)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageDeleteException(fullPath);
        }
    }

    @Override
    public void deleteMultiple(List<String> fullPaths) {
        fullPaths.forEach(this::delete);
    }

    @Override
    public String getUrl(String fullPath) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(properties.bucket())
                            .object(fullPath)
                            .method(Method.GET)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageUploadException(fullPath);
        }
    }

    @Override
    public boolean exists(String fullPath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.bucket())
                            .object(fullPath)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            throw new StorageUploadException(fullPath);
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(properties.bucket()).build()
        );

        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(properties.bucket()).build()
            );
        }
    }
}