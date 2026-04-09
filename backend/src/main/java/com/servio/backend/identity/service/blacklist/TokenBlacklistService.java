package com.servio.backend.identity.service.blacklist;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    public void revoke(String jti, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + jti,
                "revoked",
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    public boolean isRevoked(String jti) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(BLACKLIST_PREFIX + jti)
        );
    }
}