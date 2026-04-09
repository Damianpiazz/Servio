package com.servio.backend.identity.repository;

import com.servio.backend.identity.model.PasswordResetToken;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<@NotNull PasswordResetToken, @NotNull Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByToken(String token);
}