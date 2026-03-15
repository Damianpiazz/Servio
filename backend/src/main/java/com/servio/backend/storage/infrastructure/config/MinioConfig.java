package com.servio.backend.storage.infrastructure.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final ObjectStorageConfig objectStorageConfig;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(objectStorageConfig.getEndpoint())
                .credentials(objectStorageConfig.getAccessKey(), objectStorageConfig.getSecretKey())
                .build();
    }
}