package com.servio.backend.identity.repository;

import com.servio.backend.identity.model.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<@NotNull User, @NotNull Integer>, JpaSpecificationExecutor<@NotNull User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}