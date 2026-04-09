package com.servio.backend.storage.service;

import com.servio.backend.storage.model.FileToUpload;
import com.servio.backend.storage.model.UploadedFile;
import com.servio.backend.storage.validation.ImageValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private IStorageService storageService;

    @Mock
    private ImageValidator imageValidator;

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageService(storageService, imageValidator);
    }

    // ─── upload ───────────────────────────────────────────────────

    @Test
    void upload_shouldValidateAndDelegateToStorage() {
        MultipartFile file = mockJpeg("photo.jpg", 512);
        UploadedFile expected = uploadedFile("logos/1/uuid_photo.jpg");
        when(storageService.upload(any(FileToUpload.class))).thenReturn(expected);

        UploadedFile result = imageService.upload(file, "logos/1");

        verify(imageValidator).validate(file);
        verify(storageService).upload(any(FileToUpload.class));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void upload_shouldSetCorrectFolder() {
        MultipartFile file = mockJpeg("photo.jpg", 512);
        when(storageService.upload(any(FileToUpload.class))).thenReturn(uploadedFile("logos/42/x.jpg"));

        imageService.upload(file, "logos/42");

        ArgumentCaptor<FileToUpload> captor = ArgumentCaptor.forClass(FileToUpload.class);
        verify(storageService).upload(captor.capture());

        assertThat(captor.getValue().getFullPath()).startsWith("logos/42/");
    }

    @Test
    void upload_shouldGenerateUniqueFileNames() {
        MultipartFile file = mockJpeg("photo.jpg", 512);
        when(storageService.upload(any(FileToUpload.class))).thenReturn(uploadedFile("logos/1/x.jpg"));

        imageService.upload(file, "logos/1");
        imageService.upload(file, "logos/1");

        ArgumentCaptor<FileToUpload> captor = ArgumentCaptor.forClass(FileToUpload.class);
        verify(storageService, times(2)).upload(captor.capture());

        List<String> paths = captor.getAllValues().stream()
                .map(FileToUpload::getFileName)
                .toList();

        assertThat(paths.get(0)).isNotEqualTo(paths.get(1));
    }

    @Test
    void upload_shouldPassContentType() {
        MultipartFile file = mockJpeg("photo.jpg", 512);
        when(storageService.upload(any(FileToUpload.class))).thenReturn(uploadedFile("logos/1/x.jpg"));

        imageService.upload(file, "logos/1");

        ArgumentCaptor<FileToUpload> captor = ArgumentCaptor.forClass(FileToUpload.class);
        verify(storageService).upload(captor.capture());
        assertThat(captor.getValue().getContentType()).isEqualTo("image/jpeg");
    }

    // ─── uploadMultiple ───────────────────────────────────────────

    @Test
    void uploadMultiple_shouldUploadAllFiles() {
        MultipartFile file1 = mockJpeg("a.jpg", 100);
        MultipartFile file2 = mockJpeg("b.jpg", 200);
        when(storageService.upload(any())).thenReturn(uploadedFile("logos/1/x.jpg"));

        List<UploadedFile> result = imageService.uploadMultiple(List.of(file1, file2), "logos/1");

        assertThat(result).hasSize(2);
        verify(storageService, times(2)).upload(any());
    }

    // ─── delete ───────────────────────────────────────────────────

    @Test
    void delete_shouldDelegateToStorage() {
        imageService.delete("logos/1/photo.jpg");
        verify(storageService).delete("logos/1/photo.jpg");
    }

    @Test
    void deleteMultiple_shouldDeleteAllPaths() {
        imageService.deleteMultiple(List.of("logos/1/a.jpg", "logos/1/b.jpg"));
        verify(storageService).deleteMultiple(List.of("logos/1/a.jpg", "logos/1/b.jpg"));
    }

    // ─── helpers ──────────────────────────────────────────────────

    private MockMultipartFile mockJpeg(String name, int sizeBytes) {
        return new MockMultipartFile("file", name, "image/jpeg", new byte[sizeBytes]);
    }

    private UploadedFile uploadedFile(String fullPath) {
        return UploadedFile.builder()
                .fileName("photo.jpg")
                .fullPath(fullPath)
                .url("http://minio/bucket/" + fullPath)
                .contentType("image/jpeg")
                .size(512)
                .build();
    }
}