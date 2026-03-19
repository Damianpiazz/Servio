package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.in.usecase.UploadUserLogoUseCase;
import com.servio.backend.identity.application.port.out.UserLogoRepositoryPort;
import com.servio.backend.identity.domain.User;
import com.servio.backend.identity.domain.UserLogo;
import com.servio.backend.shared.image.ImageUploadService;
import com.servio.backend.storage.domain.StorageObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UploadUserLogoService implements UploadUserLogoUseCase {

    private final ImageUploadService imageUploadService;
    private final UserLogoRepositoryPort userLogoRepositoryPort;

    @Override
    public UserLogo upload(MultipartFile file, User user) {
        StorageObject uploaded = imageUploadService.upload(file, "logos/" + user.getId()); // TODO: mover a properties

        userLogoRepositoryPort.deactivateAllByUserId(user.getId());

        return userLogoRepositoryPort.save(
                UserLogo.builder()
                        .userId(user.getId())
                        .url(uploaded.getUrl())
                        .fullPath(uploaded.getFullPath())
                        .active(true)
                        .uploadedAt(LocalDateTime.now())
                        .build()
        );
    }
}