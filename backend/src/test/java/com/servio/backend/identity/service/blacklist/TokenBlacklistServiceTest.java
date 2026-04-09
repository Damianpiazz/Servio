package com.servio.backend.identity.service.blacklist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private TokenBlacklistService tokenBlacklistService;

    // ─── revoke ───────────────────────────────────────────────────

    @Test
    void revoke_shouldStoreKeyWithCorrectTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        tokenBlacklistService.revoke("jti-abc", 3600L);

        verify(valueOperations).set("blacklist:jti-abc", "revoked", 3600L, TimeUnit.SECONDS);
    }

    @Test
    void revoke_shouldUseBlacklistPrefix() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        tokenBlacklistService.revoke("my-jti", 86400L);

        verify(valueOperations).set(eq("blacklist:my-jti"), any(), anyLong(), any());
    }

    // ─── isRevoked ────────────────────────────────────────────────

    @Test
    void isRevoked_shouldReturnTrue_whenKeyExists() {
        when(redisTemplate.hasKey("blacklist:jti-abc")).thenReturn(Boolean.TRUE);

        assertThat(tokenBlacklistService.isRevoked("jti-abc")).isTrue();
    }

    @Test
    void isRevoked_shouldReturnFalse_whenKeyDoesNotExist() {
        when(redisTemplate.hasKey("blacklist:jti-abc")).thenReturn(Boolean.FALSE);

        assertThat(tokenBlacklistService.isRevoked("jti-abc")).isFalse();
    }

    @Test
    void isRevoked_shouldReturnFalse_whenRedisReturnsNull() {
        when(redisTemplate.hasKey("blacklist:jti-abc")).thenReturn(null);

        assertThat(tokenBlacklistService.isRevoked("jti-abc")).isFalse();
    }
}