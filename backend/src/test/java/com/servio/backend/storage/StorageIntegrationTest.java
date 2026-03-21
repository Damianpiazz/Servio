package com.servio.backend.storage;

import com.servio.backend.BaseIntegrationTest;
import com.servio.backend.storage.application.port.in.StorageUseCase;
import com.servio.backend.storage.domain.StorageFile;
import com.servio.backend.storage.domain.StorageObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StorageIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private StorageUseCase storageUseCase;

    @Test
    void debeSubirArchivoAMinio() {
        StorageFile file = StorageFile.builder()
                .fileName("test_foto.jpg")
                .content(new byte[]{1, 2, 3, 4, 5})
                .contentType("image/jpeg")
                .size(5)
                .folder("test")
                .build();

        StorageObject result = storageUseCase.upload(file);

        assertThat(result).isNotNull();
        assertThat(result.getUrl()).isNotBlank();
        assertThat(result.getFullPath()).isEqualTo("test/test_foto.jpg");

        storageUseCase.delete(result.getFullPath());
    }

    @Test
    void debeSubirMultiplesArchivos() {
        List<StorageFile> files = List.of(
                StorageFile.builder()
                        .fileName("test_foto1.jpg")
                        .content(new byte[]{1, 2, 3})
                        .contentType("image/jpeg")
                        .size(3)
                        .folder("test")
                        .build(),
                StorageFile.builder()
                        .fileName("test_foto2.jpg")
                        .content(new byte[]{4, 5, 6})
                        .contentType("image/jpeg")
                        .size(3)
                        .folder("test")
                        .build()
        );

        List<StorageObject> results = storageUseCase.uploadMultiple(files);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getUrl() != null);

        storageUseCase.deleteMultiple(
                results.stream().map(StorageObject::getFullPath).toList()
        );
    }

    @Test
    void debeVerificarExistenciaDeArchivo() {
        StorageFile file = StorageFile.builder()
                .fileName("test_existe.jpg")
                .content(new byte[]{1, 2, 3})
                .contentType("image/jpeg")
                .size(3)
                .folder("test")
                .build();

        storageUseCase.upload(file);
        assertThat(storageUseCase.exists("test/test_existe.jpg")).isTrue();
        assertThat(storageUseCase.exists("test/no_existe.jpg")).isFalse();

        storageUseCase.delete("test/test_existe.jpg");
    }

    @Test
    void debeEliminarArchivo() {
        StorageFile file = StorageFile.builder()
                .fileName("test_eliminar.jpg")
                .content(new byte[]{1, 2, 3})
                .contentType("image/jpeg")
                .size(3)
                .folder("test")
                .build();

        StorageObject uploaded = storageUseCase.upload(file);
        storageUseCase.delete(uploaded.getFullPath());

        assertThat(storageUseCase.exists(uploaded.getFullPath())).isFalse();
    }
}