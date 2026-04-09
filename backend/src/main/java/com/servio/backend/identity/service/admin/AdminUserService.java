package com.servio.backend.identity.service.admin;

import com.servio.backend.identity.dto.request.AdminChangePasswordRequest;
import com.servio.backend.identity.dto.request.AdminUserFilterRequest;
import com.servio.backend.identity.dto.request.ChangeUserRoleRequest;
import com.servio.backend.identity.dto.request.CreateUserRequest;
import com.servio.backend.identity.dto.response.UserResponse;
import com.servio.backend.identity.exception.EmailAlreadyExistsException;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import com.servio.backend.identity.repository.UserSpecification;
import com.servio.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .blocked(false)
                .build();

        User saved = userRepository.save(user);
        log.info("User created by admin: {}", saved.getEmail());
        return toResponse(saved);
    }

    public void blockUser(Integer userId) {
        User user = findUserOrThrow(userId);
        user.setBlocked(true);
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    public void unblockUser(Integer userId) {
        User user = findUserOrThrow(userId);
        user.setBlocked(false);
        userRepository.save(user);
        log.info("User id={} unblocked by admin", userId);
    }

    public void changeRole(Integer userId, ChangeUserRoleRequest request) {
        User user = findUserOrThrow(userId);
        user.setRole(request.getRole());
        userRepository.save(user);
        log.info("Role changed to {} for userId={}", request.getRole(), userId);
    }

    public void changePassword(Integer userId, AdminChangePasswordRequest request) {
        User user = findUserOrThrow(userId);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed by admin for userId={}", userId);
    }

    public void deleteUser(Integer userId) {
        User user = findUserOrThrow(userId);
        userRepository.delete(user);
        log.info("User id={} deleted by admin", userId);
    }

    private User findUserOrThrow(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private UserResponse toResponse(User user) {
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