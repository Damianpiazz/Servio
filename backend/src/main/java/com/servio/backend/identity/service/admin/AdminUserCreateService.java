package com.servio.backend.identity.service.admin;

import com.servio.backend.identity.dto.request.CreateUserRequest;
import com.servio.backend.identity.dto.response.UserResponse;
import com.servio.backend.identity.exception.EmailAlreadyExistsException;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserCreateService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminUserQueryService adminUserQueryService;

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
        return adminUserQueryService.toResponse(saved);
    }
}