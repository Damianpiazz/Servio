package com.servio.backend.identity.service.admin;

import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserDeleteService {

    private final UserRepository userRepository;
    private final AdminUserQueryService adminUserQueryService;

    public void deleteUser(Integer userId) {
        User user = adminUserQueryService.findUserOrThrow(userId);
        userRepository.delete(user);
        log.info("User id={} deleted by admin", userId);
    }
}