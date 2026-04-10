package com.servio.backend.audit.config;

import com.servio.backend.identity.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.javers.spring.auditable.AuthorProvider;
import org.javers.spring.auditable.CommitPropertiesProvider;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JaversAuditConfig {

    @Bean
    public AuditorAware<@NotNull Integer> auditorProvider() {
        return () -> Optional.ofNullable(
                        SecurityContextHolder.getContext().getAuthentication())
                .filter(auth -> auth.getPrincipal() instanceof UserPrincipal)
                .map(auth -> ((UserPrincipal) auth.getPrincipal()).getId());
    }

    @Bean
    public AuthorProvider authorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                return String.valueOf(principal.getId());
            }
            return "system";
        };
    }
}