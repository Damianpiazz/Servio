package com.servio.backend.identity.application.port.out;

import com.servio.backend.identity.domain.UserLogo;

import java.util.List;
import java.util.Optional;

public interface UserLogoRepositoryPort {
    UserLogo save(UserLogo logo);
    Optional<UserLogo> findById(Long id);
    Optional<UserLogo> findActiveByUserId(Integer userId);
    List<UserLogo> findAllByUserId(Integer userId);
    void deactivateAllByUserId(Integer userId);
    void delete(UserLogo logo);
}