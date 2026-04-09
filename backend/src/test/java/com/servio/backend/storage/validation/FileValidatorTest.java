package com.servio.backend.storage.validation;

import com.servio.backend.shared.exception.InvalidArgumentException;
import com.servio.backend.storage.model.FileToUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FileValidatorTest {

    private FileValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileValidator();
    }

    @Test
    void validate_shouldPass_whenFileIsValid() {
        FileToUpload file = FileToUpload.of("image.jpg", new byte[]{1, 2, 3}, "image/jpeg", 3L, "logos");
        assertThatNoException().isThrownBy(() -> validator.validate(file));
    }

    @Test
    void validate_shouldThrow_whenFileIsNull() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    void validate_shouldThrow_whenFileNameIsNull() {
        FileToUpload file = FileToUpload.of(null, new byte[]{1}, "image/jpeg", 1L, "logos");
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void validate_shouldThrow_whenFileNameIsBlank() {
        FileToUpload file = FileToUpload.of("   ", new byte[]{1}, "image/jpeg", 1L, "logos");
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void validate_shouldThrow_whenContentIsNull() {
        FileToUpload file = FileToUpload.of("img.jpg", null, "image/jpeg", 0L, "logos");
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("content");
    }

    @Test
    void validate_shouldThrow_whenContentIsEmpty() {
        FileToUpload file = FileToUpload.of("img.jpg", new byte[0], "image/jpeg", 0L, "logos");
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("content");
    }

    @Test
    void validate_shouldThrow_whenContentTypeIsNull() {
        FileToUpload file = FileToUpload.of("img.jpg", new byte[]{1}, null, 1L, "logos");
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Content type");
    }

    @Test
    void validate_shouldThrow_whenContentTypeIsBlank() {
        FileToUpload file = FileToUpload.of("img.jpg", new byte[]{1}, "  ", 1L, "logos");
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Content type");
    }
}