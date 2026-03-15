package com.servio.backend.storage.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.*;

class StorageFileTest {

    @Test
    void debeConstruirseCorrectamente() {
        StorageFile file = StorageFile.builder()
                .fileName("foto.jpg")
                .content(new byte[]{1, 2, 3})
                .contentType("image/jpeg")
                .size(3)
                .folder("publicaciones/42")
                .build();

        assertThat(file.getFileName()).isEqualTo("foto.jpg");
        assertThat(file.getContentType()).isEqualTo("image/jpeg");
        assertThat(file.getSize()).isEqualTo(3);
        assertThat(file.getFolder()).isEqualTo("publicaciones/42");
    }

    @Test
    void getFullPathDebeCombinarFolderYFileName() {
        StorageFile file = StorageFile.builder()
                .fileName("foto.jpg")
                .content(new byte[]{1, 2, 3})
                .contentType("image/jpeg")
                .size(3)
                .folder("publicaciones/42")
                .build();

        assertThat(file.getFullPath()).isEqualTo("publicaciones/42/foto.jpg");
    }

    @Test
    void getFullPathSinFolderDebeRetornarSoloFileName() {
        StorageFile file = StorageFile.builder()
                .fileName("foto.jpg")
                .content(new byte[]{1, 2, 3})
                .contentType("image/jpeg")
                .size(3)
                .build();

        assertThat(file.getFullPath()).isEqualTo("foto.jpg");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void debeFallarSiFileNameEsVacioONulo(String fileName) {
        assertThatThrownBy(() -> StorageFile.builder()
                .fileName(fileName)
                .content(new byte[]{1, 2, 3})
                .contentType("image/jpeg")
                .size(3)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre del archivo");
    }

    @Test
    void debeFallarSiContentEsVacio() {
        assertThatThrownBy(() -> StorageFile.builder()
                .fileName("foto.jpg")
                .content(new byte[]{})
                .contentType("image/jpeg")
                .size(0)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contenido");
    }

    @Test
    void debeFallarSiContentEsNulo() {
        assertThatThrownBy(() -> StorageFile.builder()
                .fileName("foto.jpg")
                .content(null)
                .contentType("image/jpeg")
                .size(0)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contenido");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void debeFallarSiContentTypeEsVacioONulo(String contentType) {
        assertThatThrownBy(() -> StorageFile.builder()
                .fileName("foto.jpg")
                .content(new byte[]{1, 2, 3})
                .contentType(contentType)
                .size(3)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tipo de contenido");
    }

    @Test
    void folderDebeDefaulutearAVacio() {
        StorageFile file = StorageFile.builder()
                .fileName("foto.jpg")
                .content(new byte[]{1, 2, 3})
                .contentType("image/jpeg")
                .size(3)
                .build();

        assertThat(file.getFolder()).isEmpty();
    }
}