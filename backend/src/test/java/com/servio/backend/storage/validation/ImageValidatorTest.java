package com.servio.backend.storage.validation;

import com.servio.backend.shared.exception.InvalidArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ImageValidatorTest {

    private ImageValidator validator;

    // 2 MB in bytes
    private static final long MAX_SIZE = 2_097_152L;
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    @BeforeEach
    void setUp() {
        validator = new ImageValidator();
        ReflectionTestUtils.setField(validator, "maxSizeBytes", MAX_SIZE);
        ReflectionTestUtils.setField(validator, "allowedTypes", ALLOWED_TYPES);
    }

    // ─── happy path ───────────────────────────────────────────────

    @Test
    void validate_shouldPass_forJpeg() {
        MultipartFile file = mockFile("photo.jpg", "image/jpeg", 1024);
        assertThatNoException().isThrownBy(() -> validator.validate(file));
    }

    @Test
    void validate_shouldPass_forPng() {
        MultipartFile file = mockFile("photo.png", "image/png", 1024);
        assertThatNoException().isThrownBy(() -> validator.validate(file));
    }

    @Test
    void validate_shouldPass_forWebp() {
        MultipartFile file = mockFile("photo.webp", "image/webp", 1024);
        assertThatNoException().isThrownBy(() -> validator.validate(file));
    }

    @Test
    void validate_shouldPass_whenFileSizeIsExactlyMaxSize() {
        MultipartFile file = mockFile("photo.jpg", "image/jpeg", (int) MAX_SIZE);
        assertThatNoException().isThrownBy(() -> validator.validate(file));
    }

    // ─── null / empty ─────────────────────────────────────────────

    @Test
    void validate_shouldThrow_whenFileIsNull() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void validate_shouldThrow_whenFileIsEmpty() {
        MultipartFile file = new MockMultipartFile("file", new byte[0]);
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("empty");
    }

    // ─── size ─────────────────────────────────────────────────────

    @Test
    void validate_shouldThrow_whenFileSizeExceedsLimit() {
        MultipartFile file = mockFile("big.jpg", "image/jpeg", (int) MAX_SIZE + 1);
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("maximum size");
    }

    // ─── content type ─────────────────────────────────────────────

    @Test
    void validate_shouldThrow_whenContentTypeIsNotAllowed() {
        MultipartFile file = mockFile("doc.pdf", "application/pdf", 1024);
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("allowed");
    }

    @Test
    void validate_shouldThrow_whenContentTypeIsGif() {
        MultipartFile file = mockFile("anim.gif", "image/gif", 1024);
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("allowed");
    }

    // ─── helper ───────────────────────────────────────────────────

    private MockMultipartFile mockFile(String name, String contentType, int sizeBytes) {
        byte[] content = new byte[sizeBytes];
        return new MockMultipartFile("file", name, contentType, content);
    }
}