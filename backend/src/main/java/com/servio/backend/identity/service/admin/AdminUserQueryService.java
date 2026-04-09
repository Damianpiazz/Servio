package com.servio.backend.identity.service.admin;

import com.servio.backend.identity.dto.request.AdminUserFilterRequest;
import com.servio.backend.identity.dto.response.UserResponse;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import com.servio.backend.identity.repository.UserSpecification;
import com.servio.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserQueryService {

    private final UserRepository userRepository;

    public Page<@NotNull UserResponse> listUsers(AdminUserFilterRequest filter, Pageable pageable) {
        return userRepository
                .findAll(UserSpecification.withFilters(filter), pageable)
                .map(this::toResponse);
    }

    public UserResponse getUserById(Integer userId) {
        return userRepository.findById(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    public User findUserOrThrow(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .role(user.getRole())
                .blocked(user.isBlocked())
                .build();
    }
}