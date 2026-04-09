package com.servio.backend.identity.facade;

import com.servio.backend.identity.dto.request.UpdateProfileRequest;
import com.servio.backend.identity.dto.response.UserResponse;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.service.logo.UserLogoService;
import com.servio.backend.identity.service.user.UserQueryService;
import com.servio.backend.identity.service.user.UserUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserQueryService userQueryService;
    private final UserUpdateService userUpdateService;
    private final UserLogoService userLogoService;

    public UserResponse getProfile(User user) {
        String activeLogoUrl = null;
        try {
            activeLogoUrl = userLogoService.getActive(user.getId()).getUrl();
        } catch (Exception ignored) {}
        return userQueryService.toResponse(user, activeLogoUrl);
    }

    public UserResponse updateProfile(UpdateProfileRequest request, User user) {
        return userUpdateService.updateProfile(request, user);
    }
}