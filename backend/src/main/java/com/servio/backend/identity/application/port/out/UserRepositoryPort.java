package com.servio.backend.identity.application.port.out;

import com.servio.backend.identity.domain.User;

import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Integer id);
    boolean existsByEmail(String email);
}