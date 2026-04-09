package com.servio.backend.identity.service.user;

import com.servio.backend.identity.dto.request.UpdateProfileRequest;
import com.servio.backend.identity.dto.response.UserResponse;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse updateProfile(UpdateProfileRequest request, User user) {
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        User saved = userRepository.save(user);
        log.info("Profile updated for userId={}", user.getId());
        return toResponse(saved, null);
    }

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