package com.servio.backend.identity.service.admin;

import com.servio.backend.identity.dto.request.ChangeUserRoleRequest;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserRoleService {

    private final UserRepository userRepository;
    private final AdminUserQueryService adminUserQueryService;

    public void changeRole(Integer userId, ChangeUserRoleRequest request) {
        User user = adminUserQueryService.findUserOrThrow(userId);
        user.setRole(request.getRole());
        userRepository.save(user);
        log.info("Role changed to {} for userId={}", request.getRole(), userId);
    }
}