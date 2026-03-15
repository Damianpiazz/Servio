package com.servio.backend.storage.application.service;

import com.servio.backend.storage.application.port.out.StoragePort;
import com.servio.backend.storage.domain.StorageFile;
import com.servio.backend.storage.domain.StorageObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private StorageService storageService;

    @Test
    void debeSubirArchivoYDevolverStorageObject() {
        StorageFile file = buildStorageFile("foto.jpg", "publicaciones/42");
        StorageObject expected = buildStorageObject("foto.jpg", "publicaciones/42");
        when(storagePort.upload(file)).thenReturn(expected);

        StorageObject result = storageService.upload(file);

        assertThat(result).isEqualTo(expected);
        verify(storagePort, times(1)).upload(file);
    }

    @Test
    void debeSubirMultiplesArchivos() {
        StorageFile file1 = buildStorageFile("foto1.jpg", "publicaciones/42");
        StorageFile file2 = buildStorageFile("foto2.jpg", "publicaciones/42");
        StorageObject obj1 = buildStorageObject("foto1.jpg", "publicaciones/42");
        StorageObject obj2 = buildStorageObject("foto2.jpg", "publicaciones/42");

        when(storagePort.upload(file1)).thenReturn(obj1);
        when(storagePort.upload(file2)).thenReturn(obj2);

        List<StorageObject> results = storageService.uploadMultiple(List.of(file1, file2));

        assertThat(results).hasSize(2);
        verify(storagePort, times(2)).upload(any());
    }

    @Test
    void debeEliminarArchivo() {
        storageService.delete("publicaciones/42/foto.jpg");

        verify(storagePort, times(1)).delete("publicaciones/42/foto.jpg");
    }

    @Test
    void debeEliminarMultiplesArchivos() {
        List<String> paths = List.of(
                "publicaciones/42/foto1.jpg",
                "publicaciones/42/foto2.jpg",
                "publicaciones/42/foto3.jpg"
        );

        storageService.deleteMultiple(paths);

        verify(storagePort, times(3)).delete(any());
    }

    @Test
    void debeVerificarExistencia() {
        when(storagePort.exists("publicaciones/42/foto.jpg")).thenReturn(true);

        boolean exists = storageService.exists("publicaciones/42/foto.jpg");

        assertThat(exists).isTrue();
        verify(storagePort, times(1)).exists("publicaciones/42/foto.jpg");
    }

    @Test
    void debeObtenerUrl() {
        String expectedUrl = "https://minio/servio-assets/publicaciones/42/foto.jpg";
        when(storagePort.getUrl("publicaciones/42/foto.jpg")).thenReturn(expectedUrl);

        String url = storageService.getUrl("publicaciones/42/foto.jpg");

        assertThat(url).isEqualTo(expectedUrl);
    }

    @Test
    void debePropagarlExcepcionDelAdapter() {
        StorageFile file = buildStorageFile("foto.jpg", "publicaciones/42");
        when(storagePort.upload(any())).thenThrow(new RuntimeException("MinIO caído"));

        assertThatThrownBy(() -> storageService.upload(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("MinIO caído");
    }

    @Test
    void uploadMultipleDebeRetornarListaVaciaSiNoHayArchivos() {
        List<StorageObject> results = storageService.uploadMultiple(List.of());

        assertThat(results).isEmpty();
        verify(storagePort, never()).upload(any());
    }

    // --- helpers ---

    private StorageFile buildStorageFile(String fileName, String folder) {
        return StorageFile.builder()
                .fileName(fileName)
                .content(new byte[]{1, 2, 3})
                .contentType("image/jpeg")
                .size(3)
                .folder(folder)
                .build();
    }

    private StorageObject buildStorageObject(String fileName, String folder) {
        return StorageObject.builder()
                .fileName(fileName)
                .fullPath(folder + "/" + fileName)
                .url("https://minio/servio-assets/" + folder + "/" + fileName)
                .contentType("image/jpeg")
                .size(3)
                .build();
    }
}