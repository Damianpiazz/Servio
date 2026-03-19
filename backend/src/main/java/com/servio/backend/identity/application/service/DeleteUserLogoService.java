package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.in.usecase.DeleteUserLogoUseCase;
import com.servio.backend.identity.application.port.out.UserLogoRepositoryPort;
import com.servio.backend.identity.domain.User;
import com.servio.backend.identity.domain.UserLogo;
import com.servio.backend.shared.exception.ForbiddenException;
import com.servio.backend.shared.exception.ResourceNotFoundException;
import com.servio.backend.shared.image.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteUserLogoService implements DeleteUserLogoUseCase {

    private final UserLogoRepositoryPort userLogoRepositoryPort;
    private final ImageUploadService imageUploadService;

    @Override
    public void delete(Long logoId, User user) {
        UserLogo logo = userLogoRepositoryPort.findById(logoId)
                .orElseThrow(() -> new ResourceNotFoundException("Logo", "id", logoId));

        if (!logo.getUserId().equals(user.getId())) {
            throw new ForbiddenException("No tenés permiso para eliminar este logo");
        }

        imageUploadService.delete(logo.getFullPath());
        userLogoRepositoryPort.delete(logo);
    }
}