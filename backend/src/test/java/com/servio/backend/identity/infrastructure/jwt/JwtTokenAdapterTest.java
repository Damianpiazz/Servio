package com.servio.backend.identity.infrastructure.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtTokenAdapterTest {

    private JwtTokenAdapter jwtTokenAdapter;

    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION = 86400000L;
    private static final long REFRESH_EXPIRATION = 604800000L;

    @BeforeEach
    void setUp() {
        jwtTokenAdapter = new JwtTokenAdapter();
        ReflectionTestUtils.setField(jwtTokenAdapter, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtTokenAdapter, "jwtExpiration", EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenAdapter, "refreshExpiration", REFRESH_EXPIRATION);
    }

    @Test
    void debeGenerarAccessTokenCorrectamente() {
        String token = jwtTokenAdapter.generateAccessToken("juan@test.com", "USER");

        assertThat(token).isNotBlank();
        assertThat(jwtTokenAdapter.extractEmail(token)).isEqualTo("juan@test.com");
        assertThat(jwtTokenAdapter.extractRole(token)).isEqualTo("USER");
    }

    @Test
    void debeGenerarRefreshTokenCorrectamente() {
        String token = jwtTokenAdapter.generateRefreshToken("juan@test.com");

        assertThat(token).isNotBlank();
        assertThat(jwtTokenAdapter.extractEmail(token)).isEqualTo("juan@test.com");
    }

    @Test
    void debeValidarTokenCorrectamente() {
        String token = jwtTokenAdapter.generateAccessToken("juan@test.com", "USER");

        assertThat(jwtTokenAdapter.isTokenValid(token, "juan@test.com")).isTrue();
    }

    @Test
    void debeDetectarTokenConEmailIncorrecto() {
        String token = jwtTokenAdapter.generateAccessToken("juan@test.com", "USER");

        assertThat(jwtTokenAdapter.isTokenValid(token, "otro@test.com")).isFalse();
    }

    @Test
    void debeRetornarTtlEnSegundos() {
        assertThat(jwtTokenAdapter.getAccessTokenTtlSeconds()).isEqualTo(86400L);
    }
}