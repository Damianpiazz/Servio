package com.servio.backend.identity.service.admin;

import com.servio.backend.identity.dto.request.AdminChangePasswordRequest;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminUserQueryService adminUserQueryService;

    public void changePassword(Integer userId, AdminChangePasswordRequest request) {
        User user = adminUserQueryService.findUserOrThrow(userId);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed by admin for userId={}", userId);
    }
}