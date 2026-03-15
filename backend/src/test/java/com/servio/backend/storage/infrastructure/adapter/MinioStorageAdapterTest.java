package com.servio.backend.storage.infrastructure.adapter;

import com.servio.backend.storage.domain.StorageFile;
import com.servio.backend.storage.domain.StorageObject;
import com.servio.backend.storage.infrastructure.config.ObjectStorageConfig;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioStorageAdapterTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private ObjectStorageConfig objectStorageConfig;

    @InjectMocks
    private MinioStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        when(objectStorageConfig.getBucket()).thenReturn("servio-assets");
    }

    @Test
    void debeSubirArchivoYDevolverStorageObject() throws Exception {
        StorageFile file = buildStorageFile();
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("https://minio/servio-assets/publicaciones/42/foto.jpg");

        StorageObject result = adapter.upload(file);

        assertThat(result.getFileName()).isEqualTo("foto.jpg");
        assertThat(result.getFullPath()).isEqualTo("publicaciones/42/foto.jpg");
        assertThat(result.getUrl()).contains("foto.jpg");
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void debeCrearBucketSiNoExiste() throws Exception {
        StorageFile file = buildStorageFile();
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("https://minio/servio-assets/publicaciones/42/foto.jpg");

        adapter.upload(file);

        verify(minioClient, times(1)).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void debeEliminarArchivo() throws Exception {
        adapter.delete("publicaciones/42/foto.jpg");

        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void debeRetornarTrueSiArchivoExiste() throws Exception {
        when(minioClient.statObject(any(StatObjectArgs.class)))
                .thenReturn(mock(StatObjectResponse.class));

        boolean exists = adapter.exists("publicaciones/42/foto.jpg");

        assertThat(exists).isTrue();
    }

    @Test
    void debeRetornarFalseSiArchivoNoExiste() throws Exception {
        when(minioClient.statObject(any(StatObjectArgs.class)))
                .thenThrow(mock(ErrorResponseException.class));

        boolean exists = adapter.exists("publicaciones/42/foto.jpg");

        assertThat(exists).isFalse();
    }

    @Test
    void debeLanzarExcepcionSiFallaElUpload() throws Exception {
        StorageFile file = buildStorageFile();
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        doThrow(new RuntimeException("MinIO caído"))
                .when(minioClient).putObject(any(PutObjectArgs.class));

        assertThatThrownBy(() -> adapter.upload(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo subir el archivo");
    }

    // --- helper ---

    private StorageFile buildStorageFile() {
        return StorageFile.builder()
                .fileName("foto.jpg")
                .content(new byte[]{1, 2, 3})
                .contentType("image/jpeg")
                .size(3)
                .folder("publicaciones/42")
                .build();
    }
}