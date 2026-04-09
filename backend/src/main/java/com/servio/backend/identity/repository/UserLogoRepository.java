package com.servio.backend.identity.repository;

import com.servio.backend.identity.model.UserLogo;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserLogoRepository extends JpaRepository<@NotNull UserLogo, @NotNull Long> {
    Optional<UserLogo> findByUserIdAndActiveTrue(Integer userId);
    List<UserLogo> findAllByUserIdOrderByUploadedAtDesc(Integer userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserLogo l SET l.active = false WHERE l.user.id = :userId")
    void deactivateAllByUserId(Integer userId);
}