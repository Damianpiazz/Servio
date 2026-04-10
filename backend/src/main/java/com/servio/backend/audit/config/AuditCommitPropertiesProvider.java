package com.servio.backend.audit.config;

import com.servio.backend.identity.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.javers.spring.auditable.CommitPropertiesProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuditCommitPropertiesProvider implements CommitPropertiesProvider {

    @Override
    public Map<String, String> provideForCommittedObject(Object domainObject) {
        Map<String, String> props = new HashMap<>();

        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes();

            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                props.put("ip",        resolveClientIp(request));
                props.put("userAgent", request.getHeader("User-Agent"));
                props.put("requestId", resolveRequestId(request));
                props.put("endpoint",  request.getRequestURI());
                props.put("method",    request.getMethod());
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                props.put("role", principal.getAuthorities().stream()
                        .findFirst()
                        .map(a -> a.getAuthority().replace("ROLE_", ""))
                        .orElse("unknown"));
            }

        } catch (Exception ignored) {}

        return props;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return (requestId != null && !requestId.isBlank()) ? requestId : "unknown";
    }
}