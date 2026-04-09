package com.servio.backend.identity.facade;

import com.servio.backend.identity.dto.response.UserLogoResponse;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.model.UserLogo;
import com.servio.backend.identity.service.logo.UserLogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserLogoFacade {

    private final UserLogoService userLogoService;

    public UserLogo upload(MultipartFile file, User user) {
        return userLogoService.upload(file, user);
    }

    public void delete(Long logoId, User user) {
        userLogoService.delete(logoId, user);
    }

    public List<UserLogoResponse> getHistory(Integer userId) {
        return userLogoService.getHistory(userId);
    }

    public UserLogoResponse getActive(Integer userId) {
        return userLogoService.getActive(userId);
    }
}