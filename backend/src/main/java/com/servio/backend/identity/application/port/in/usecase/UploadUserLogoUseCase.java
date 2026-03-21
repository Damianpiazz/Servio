package com.servio.backend.identity.application.port.in.usecase;

import com.servio.backend.identity.domain.User;
import com.servio.backend.identity.domain.UserLogo;
import org.springframework.web.multipart.MultipartFile;

public interface UploadUserLogoUseCase {
    UserLogo upload(MultipartFile file, User user);
}