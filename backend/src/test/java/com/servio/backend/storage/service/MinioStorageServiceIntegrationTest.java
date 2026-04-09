package com.servio.backend.storage.service;

import com.servio.backend.storage.config.StorageProperties;
import com.servio.backend.storage.exception.StorageDeleteException;
import com.servio.backend.storage.model.FileToUpload;
import com.servio.backend.storage.model.UploadedFile;
import com.servio.backend.storage.validation.FileValidator;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
class MinioStorageServiceIntegrationTest {

    private static final String ACCESS_KEY = "minioadmin";
    private static final String SECRET_KEY = "minioadmin";
    private static final String BUCKET     = "test-bucket";

    @Container
    static MinIOContainer minio = new MinIOContainer("minio/minio:latest")
            .withUserName(ACCESS_KEY)
            .withPassword(SECRET_KEY);

    private MinioStorageService storageService;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties(
                minio.getS3URL(), ACCESS_KEY, SECRET_KEY, BUCKET
        );

        MinioClient client = MinioClient.builder()
                .endpoint(minio.getS3URL())
                .credentials(ACCESS_KEY, SECRET_KEY)
                .build();

        storageService = new MinioStorageService(client, props, new FileValidator());
    }

    // ─── upload ───────────────────────────────────────────────────

    @Test
    void upload_shouldStoreFileAndReturnMetadata() {
        FileToUpload file = sampleFile("hello.jpg", "logos/1");

        UploadedFile result = storageService.upload(file);

        assertThat(result.getFullPath()).isEqualTo("logos/1/hello.jpg");
        assertThat(result.getContentType()).isEqualTo("image/jpeg");
        assertThat(result.getSize()).isEqualTo(file.getContent().length);
        assertThat(result.getUrl()).isNotBlank();
    }

    @Test
    void upload_shouldCreateBucketIfNotExists() {
        // bucket doesn't exist at start; upload should auto-create it
        FileToUpload file = sampleFile("auto-bucket.jpg", "misc");
        assertThatNoException().isThrownBy(() -> storageService.upload(file));
    }

    // ─── exists ───────────────────────────────────────────────────

    @Test
    void exists_shouldReturnTrue_afterUpload() {
        FileToUpload file = sampleFile("exist-check.jpg", "logos/2");
        storageService.upload(file);

        assertThat(storageService.exists("logos/2/exist-check.jpg")).isTrue();
    }

    @Test
    void exists_shouldReturnFalse_whenFileNotPresent() {
        assertThat(storageService.exists("logos/999/ghost.jpg")).isFalse();
    }

    // ─── download ─────────────────────────────────────────────────

    @Test
    void download_shouldReturnInputStream_afterUpload() throws Exception {
        FileToUpload file = sampleFile("download-me.jpg", "logos/3");
        storageService.upload(file);

        try (InputStream is = storageService.download("logos/3/download-me.jpg")) {
            assertThat(is).isNotNull();
            assertThat(is.readAllBytes()).isEqualTo(file.getContent());
        }
    }

    // ─── delete ───────────────────────────────────────────────────

    @Test
    void delete_shouldRemoveFile() {
        FileToUpload file = sampleFile("to-delete.jpg", "logos/4");
        storageService.upload(file);

        storageService.delete("logos/4/to-delete.jpg");

        assertThat(storageService.exists("logos/4/to-delete.jpg")).isFalse();
    }

    @Test
    void deleteMultiple_shouldRemoveAllFiles() {
        storageService.upload(sampleFile("a.jpg", "logos/5"));
        storageService.upload(sampleFile("b.jpg", "logos/5"));

        storageService.deleteMultiple(List.of("logos/5/a.jpg", "logos/5/b.jpg"));

        assertThat(storageService.exists("logos/5/a.jpg")).isFalse();
        assertThat(storageService.exists("logos/5/b.jpg")).isFalse();
    }

    // ─── getUrl ───────────────────────────────────────────────────

    @Test
    void getUrl_shouldReturnPresignedUrl_afterUpload() {
        FileToUpload file = sampleFile("url-test.jpg", "logos/6");
        storageService.upload(file);

        String url = storageService.getUrl("logos/6/url-test.jpg");

        assertThat(url).contains("url-test.jpg");
    }

    // ─── uploadMultiple ───────────────────────────────────────────

    @Test
    void uploadMultiple_shouldUploadAllFiles() {
        List<FileToUpload> files = List.of(
                sampleFile("m1.jpg", "logos/7"),
                sampleFile("m2.jpg", "logos/7")
        );

        List<UploadedFile> results = storageService.uploadMultiple(files);

        assertThat(results).hasSize(2);
        assertThat(storageService.exists("logos/7/m1.jpg")).isTrue();
        assertThat(storageService.exists("logos/7/m2.jpg")).isTrue();
    }

    // ─── helper ───────────────────────────────────────────────────

    private FileToUpload sampleFile(String name, String folder) {
        byte[] content = ("fake-image-content-" + name).getBytes();
        return FileToUpload.of(name, content, "image/jpeg", content.length, folder);
    }
}