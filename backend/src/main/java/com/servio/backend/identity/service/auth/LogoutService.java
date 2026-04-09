package com.servio.backend.identity.service.auth;

import com.servio.backend.identity.service.blacklist.TokenBlacklistService;
import com.servio.backend.identity.service.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService {

    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    public void logout(String token) {
        String jti = jwtService.extractJti(token);

        tokenBlacklistService.revoke(
                jti,
                jwtService.getAccessTokenTtlSeconds()
        );

        log.info("Access token revoked jti={}", jti);
    }
}