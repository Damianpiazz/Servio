package com.servio.backend.config.logging;

import com.servio.backend.identity.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_KEY = "userId";
    private static final String METHOD_KEY = "method";
    private static final String PATH_KEY = "path";

    private static final Set<String> SILENT_PATHS = Set.of("/actuator/health");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SILENT_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            MDC.put(REQUEST_ID_KEY, generateRequestId(request));
            MDC.put(METHOD_KEY, request.getMethod());
            MDC.put(PATH_KEY, request.getRequestURI());
            setUserIdIfAuthenticated();

            long start = System.currentTimeMillis();
            filterChain.doFilter(request, response);
            long duration = System.currentTimeMillis() - start;

            log.info("{} {} → {} ({}ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration);
        } finally {
            MDC.clear();
        }
    }

    private String generateRequestId(HttpServletRequest request) {
        String existing = request.getHeader("X-Request-Id");
        return (existing != null && !existing.isBlank())
                ? existing
                : UUID.randomUUID().toString().substring(0, 8);
    }

    private void setUserIdIfAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof UserPrincipal principal) {
            MDC.put(USER_KEY, String.valueOf(principal.getId()));
        }
    }
}