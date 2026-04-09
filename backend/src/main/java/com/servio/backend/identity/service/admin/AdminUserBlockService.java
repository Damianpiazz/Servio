package com.servio.backend.identity.service.admin;

import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserBlockService {

    private final UserRepository userRepository;
    private final AdminUserQueryService adminUserQueryService;

    public void blockUser(Integer userId) {
        User user = adminUserQueryService.findUserOrThrow(userId);
        user.setBlocked(true);
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        log.info("User id={} blocked by admin", userId);
    }

    public void unblockUser(Integer userId) {
        User user = adminUserQueryService.findUserOrThrow(userId);
        user.setBlocked(false);
        userRepository.save(user);
        log.info("User id={} unblocked by admin", userId);
    }
}