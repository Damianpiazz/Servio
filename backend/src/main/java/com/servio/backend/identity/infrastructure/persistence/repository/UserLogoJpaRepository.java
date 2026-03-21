package com.servio.backend.identity.infrastructure.persistence.repository;

import com.servio.backend.identity.infrastructure.persistence.entity.UserLogoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserLogoJpaRepository extends JpaRepository<UserLogoEntity, Long> {
    Optional<UserLogoEntity> findByUserIdAndActiveTrue(Integer userId);
    List<UserLogoEntity> findAllByUserIdOrderByUploadedAtDesc(Integer userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserLogoEntity l SET l.active = false WHERE l.user.id = :userId")
    void deactivateAllByUserId(Integer userId);
}