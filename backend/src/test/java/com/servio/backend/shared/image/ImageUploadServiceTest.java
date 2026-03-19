package com.servio.backend.shared.image;

import com.servio.backend.shared.exception.InvalidArgumentException;
import com.servio.backend.storage.application.port.in.StorageUseCase;
import com.servio.backend.storage.domain.StorageObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    @Mock
    private StorageUseCase storageUseCase;

    @InjectMocks
    private ImageUploadService imageUploadService;

    @BeforeEach
    void setUp() {
        // inyectamos los @Value manualmente ya que no hay Spring en unit tests
        ReflectionTestUtils.setField(imageUploadService, "maxSizeBytes", 2097152L);
        ReflectionTestUtils.setField(imageUploadService, "allowedTypes", List.of("image/jpeg", "image/png", "image/webp"));
    }

    // --- upload ---

    @Test
    void debeSubirImagenCorrectamente() {
        MockMultipartFile file = buildFile("foto.jpg", "image/jpeg", 1024);
        StorageObject expected = buildStorageObject("logos/1/uuid_foto.jpg");
        when(storageUseCase.upload(any())).thenReturn(expected);

        StorageObject result = imageUploadService.upload(file, "logos/1");

        assertThat(result).isEqualTo(expected);
        verify(storageUseCase, times(1)).upload(any());
    }

    @Test
    void debeFallarSiArchivoEstaVacio() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> imageUploadService.upload(file, "logos/1"))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("vacío");
    }

    @Test
    void debeFallarSiArchivoSuperaElTamanioMaximo() {
        MockMultipartFile file = buildFile("foto.jpg", "image/jpeg", 3 * 1024 * 1024); // 3MB

        assertThatThrownBy(() -> imageUploadService.upload(file, "logos/1"))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("tamaño máximo");
    }

    @Test
    void debeFallarSiTipoNoEstaPermitido() {
        MockMultipartFile file = buildFile("documento.pdf", "application/pdf", 1024);

        assertThatThrownBy(() -> imageUploadService.upload(file, "logos/1"))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Solo se permiten");
    }

    @Test
    void debeAceptarJpeg() {
        MockMultipartFile file = buildFile("foto.jpg", "image/jpeg", 1024);
        when(storageUseCase.upload(any())).thenReturn(buildStorageObject("logos/1/foto.jpg"));

        assertThatNoException().isThrownBy(() -> imageUploadService.upload(file, "logos/1"));
    }

    @Test
    void debeAceptarPng() {
        MockMultipartFile file = buildFile("foto.png", "image/png", 1024);
        when(storageUseCase.upload(any())).thenReturn(buildStorageObject("logos/1/foto.png"));

        assertThatNoException().isThrownBy(() -> imageUploadService.upload(file, "logos/1"));
    }

    @Test
    void debeAceptarWebp() {
        MockMultipartFile file = buildFile("foto.webp", "image/webp", 1024);
        when(storageUseCase.upload(any())).thenReturn(buildStorageObject("logos/1/foto.webp"));

        assertThatNoException().isThrownBy(() -> imageUploadService.upload(file, "logos/1"));
    }

    // --- uploadMultiple ---

    @Test
    void debeSubirMultiplesImagenes() {
        MockMultipartFile file1 = buildFile("foto1.jpg", "image/jpeg", 1024);
        MockMultipartFile file2 = buildFile("foto2.jpg", "image/jpeg", 1024);
        when(storageUseCase.upload(any()))
                .thenReturn(buildStorageObject("publicaciones/1/foto1.jpg"))
                .thenReturn(buildStorageObject("publicaciones/1/foto2.jpg"));

        List<StorageObject> results = imageUploadService.uploadMultiple(List.of(file1, file2), "publicaciones/1");

        assertThat(results).hasSize(2);
        verify(storageUseCase, times(2)).upload(any());
    }

    @Test
    void debeFallarSiListaDeArchivosEsVacia() {
        assertThatThrownBy(() -> imageUploadService.uploadMultiple(List.of(), "publicaciones/1"))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("al menos una imagen");
    }

    @Test
    void debeFallarSiListaDeArchivosEsNula() {
        assertThatThrownBy(() -> imageUploadService.uploadMultiple(null, "publicaciones/1"))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("al menos una imagen");
    }

    // --- delete ---

    @Test
    void debeEliminarImagenCorrectamente() {
        imageUploadService.delete("logos/1/foto.jpg");

        verify(storageUseCase, times(1)).delete("logos/1/foto.jpg");
    }

    @Test
    void debeEliminarMultiplesImagenes() {
        List<String> paths = List.of("logos/1/foto1.jpg", "logos/1/foto2.jpg");

        imageUploadService.deleteMultiple(paths);

        verify(storageUseCase, times(1)).deleteMultiple(paths);
    }

    // --- helpers ---

    private MockMultipartFile buildFile(String filename, String contentType, int sizeBytes) {
        return new MockMultipartFile(
                "file",
                filename,
                contentType,
                new byte[sizeBytes]
        );
    }

    private StorageObject buildStorageObject(String fullPath) {
        return StorageObject.builder()
                .fileName(fullPath.substring(fullPath.lastIndexOf("/") + 1))
                .fullPath(fullPath)
                .url("https://minio/servio-assets/" + fullPath)
                .contentType("image/jpeg")
                .size(1024)
                .build();
    }
}