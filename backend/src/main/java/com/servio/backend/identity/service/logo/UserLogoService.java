package com.servio.backend.identity.service.logo;

import com.servio.backend.identity.dto.response.UserLogoResponse;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.model.UserLogo;
import com.servio.backend.identity.repository.UserLogoRepository;
import com.servio.backend.shared.exception.AppException;
import com.servio.backend.shared.exception.ResourceNotFoundException;
import com.servio.backend.storage.model.UploadedFile;
import com.servio.backend.storage.service.IImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLogoService {

    private final UserLogoRepository userLogoRepository;
    private final IImageService imageService;

    @Value("${identity.user.logo.folder:logos}")
    private String logoFolder;

    public UserLogo upload(MultipartFile file, User user) {
        UploadedFile uploaded = imageService.upload(file, logoFolder + "/" + user.getId());
        userLogoRepository.deactivateAllByUserId(user.getId());

        UserLogo logo = UserLogo.builder()
                .user(user)
                .url(uploaded.getUrl())
                .fullPath(uploaded.getFullPath())
                .active(true)
                .uploadedAt(LocalDateTime.now())
                .build();

        return userLogoRepository.save(logo);
    }

    public void delete(Long logoId, User user) {
        UserLogo logo = userLogoRepository.findById(logoId)
                .orElseThrow(() -> new ResourceNotFoundException("Logo", "id", logoId));

        if (!logo.getUser().getId().equals(user.getId())) {
            throw new AppException("You don't have permission to delete this logo",
                    HttpStatus.FORBIDDEN) {};
        }

        imageService.delete(logo.getFullPath());
        userLogoRepository.delete(logo);
        log.info("Logo id={} deleted for userId={}", logoId, user.getId());
    }

    public List<UserLogoResponse> getHistory(Integer userId) {
        return userLogoRepository.findAllByUserIdOrderByUploadedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserLogoResponse getActive(Integer userId) {
        return userLogoRepository.findByUserIdAndActiveTrue(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Active logo", "userId", userId));
    }

    private UserLogoResponse toResponse(UserLogo logo) {
        return UserLogoResponse.builder()
                .id(logo.getId())
                .url(logo.getUrl())
                .active(logo.isActive())
                .uploadedAt(logo.getUploadedAt())
                .build();
    }
}