package com.servio.backend.identity.service.user;

import com.servio.backend.identity.dto.response.UserResponse;
import com.servio.backend.identity.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    public UserResponse toResponse(User user, String activeLogoUrl) {
        return UserResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .role(user.getRole())
                .blocked(user.isBlocked())
                .activeLogoUrl(activeLogoUrl)
                .build();
    }
}