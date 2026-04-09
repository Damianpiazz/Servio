package com.servio.backend.identity.service.logo;

import com.servio.backend.identity.dto.response.UserLogoResponse;
import com.servio.backend.identity.model.Role;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.model.UserLogo;
import com.servio.backend.identity.repository.UserLogoRepository;
import com.servio.backend.shared.exception.AppException;
import com.servio.backend.shared.exception.ResourceNotFoundException;
import com.servio.backend.storage.model.UploadedFile;
import com.servio.backend.storage.service.IImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLogoServiceTest {

    @Mock private UserLogoRepository userLogoRepository;
    @Mock private IImageService imageService;

    @InjectMocks private UserLogoService userLogoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userLogoService, "logoFolder", "logos");
    }

    // ─── upload ───────────────────────────────────────────────────

    @Test
    void upload_shouldDeactivatePreviousLogosAndSaveNew() {
        User user = user();
        MultipartFile file = mockJpeg();
        UploadedFile uploaded = uploadedFile();

        when(imageService.upload(eq(file), eq("logos/1"))).thenReturn(uploaded);
        when(userLogoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserLogo result = userLogoService.upload(file, user);

        verify(userLogoRepository).deactivateAllByUserId(1);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUrl()).isEqualTo("http://minio/logo.jpg");
        assertThat(result.getFullPath()).isEqualTo("logos/1/logo.jpg");
    }

    @Test
    void upload_shouldSetUploadedAtTimestamp() {
        User user = user();
        when(imageService.upload(any(), any())).thenReturn(uploadedFile());
        when(userLogoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserLogo result = userLogoService.upload(mockJpeg(), user);

        assertThat(result.getUploadedAt()).isNotNull();
        assertThat(result.getUploadedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    // ─── delete ───────────────────────────────────────────────────

    @Test
    void delete_shouldDeleteLogo_whenOwnerMatches() {
        User user = user();
        UserLogo logo = logo(1L, user);
        when(userLogoRepository.findById(1L)).thenReturn(Optional.of(logo));

        userLogoService.delete(1L, user);

        verify(imageService).delete(logo.getFullPath());
        verify(userLogoRepository).delete(logo);
    }

    @Test
    void delete_shouldThrow_whenLogoNotFound() {
        when(userLogoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userLogoService.delete(999L, user()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldThrow_whenUserIsNotOwner() {
        User owner = user();
        User other = User.builder().id(99).email("other@example.com").role(Role.USER).build();
        UserLogo logo = logo(1L, owner);
        when(userLogoRepository.findById(1L)).thenReturn(Optional.of(logo));

        assertThatThrownBy(() -> userLogoService.delete(1L, other))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("permission");

        verify(imageService, never()).delete(any());
        verify(userLogoRepository, never()).delete(any());
    }

    // ─── getHistory ───────────────────────────────────────────────

    @Test
    void getHistory_shouldReturnAllLogosOrderedByDate() {
        User user = user();
        List<UserLogo> logos = List.of(logo(2L, user), logo(1L, user));
        when(userLogoRepository.findAllByUserIdOrderByUploadedAtDesc(1)).thenReturn(logos);

        List<UserLogoResponse> history = userLogoService.getHistory(1);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getId()).isEqualTo(2L);
    }

    @Test
    void getHistory_shouldReturnEmptyList_whenNoLogos() {
        when(userLogoRepository.findAllByUserIdOrderByUploadedAtDesc(1)).thenReturn(List.of());

        List<UserLogoResponse> history = userLogoService.getHistory(1);

        assertThat(history).isEmpty();
    }

    // ─── getActive ────────────────────────────────────────────────

    @Test
    void getActive_shouldReturnActiveLogo() {
        User user = user();
        UserLogo active = logo(5L, user);
        active.setActive(true);
        when(userLogoRepository.findByUserIdAndActiveTrue(1)).thenReturn(Optional.of(active));

        UserLogoResponse response = userLogoService.getActive(1);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void getActive_shouldThrow_whenNoActiveLogo() {
        when(userLogoRepository.findByUserIdAndActiveTrue(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userLogoService.getActive(1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── helpers ──────────────────────────────────────────────────

    private User user() {
        return User.builder()
                .id(1).email("john@example.com").firstname("John")
                .role(Role.USER).blocked(false).tokenVersion(0).build();
    }

    private UserLogo logo(Long id, User user) {
        return UserLogo.builder()
                .id(id).user(user)
                .url("http://minio/logo.jpg")
                .fullPath("logos/1/logo.jpg")
                .active(false)
                .uploadedAt(LocalDateTime.now().minusDays(id))
                .build();
    }

    private UploadedFile uploadedFile() {
        return UploadedFile.builder()
                .fileName("logo.jpg")
                .fullPath("logos/1/logo.jpg")
                .url("http://minio/logo.jpg")
                .contentType("image/jpeg")
                .size(1024)
                .build();
    }

    private MockMultipartFile mockJpeg() {
        return new MockMultipartFile("file", "logo.jpg", "image/jpeg", new byte[512]);
    }
}