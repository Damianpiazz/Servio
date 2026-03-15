package com.servio.backend.storage.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StorageObjectTest {

    @Test
    void debeConstruirseCorrectamente() {
        StorageObject object = StorageObject.builder()
                .fileName("foto.jpg")
                .fullPath("publicaciones/42/foto.jpg")
                .url("https://minio/servio-assets/publicaciones/42/foto.jpg")
                .contentType("image/jpeg")
                .size(3)
                .build();

        assertThat(object.getFileName()).isEqualTo("foto.jpg");
        assertThat(object.getFullPath()).isEqualTo("publicaciones/42/foto.jpg");
        assertThat(object.getUrl()).isEqualTo("https://minio/servio-assets/publicaciones/42/foto.jpg");
        assertThat(object.getContentType()).isEqualTo("image/jpeg");
        assertThat(object.getSize()).isEqualTo(3);
    }
}