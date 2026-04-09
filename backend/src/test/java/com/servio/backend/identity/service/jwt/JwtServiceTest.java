package com.servio.backend.identity.service.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    // 256-bit base64 key (same as test properties)
    private static final String SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long ACCESS_EXP  = 86_400_000L; // 24h in ms
    private static final long REFRESH_EXP = 604_800_000L; // 7d in ms

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", ACCESS_EXP);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", REFRESH_EXP);
    }

    // ─── generateAccessToken ──────────────────────────────────────

    @Test
    void generateAccessToken_shouldContainEmailAndRole() {
        String token = jwtService.generateAccessToken("john@example.com", "USER", 0);

        assertThat(jwtService.extractEmail(token)).isEqualTo("john@example.com");
    }

    @Test
    void generateAccessToken_shouldBeValid() {
        String token = jwtService.generateAccessToken("john@example.com", "USER", 0);

        assertThat(jwtService.isTokenValid(token, "john@example.com")).isTrue();
    }

    @Test
    void generateAccessToken_shouldNotBeRefreshToken() {
        String token = jwtService.generateAccessToken("john@example.com", "USER", 0);

        assertThat(jwtService.isRefreshToken(token)).isFalse();
    }

    @Test
    void generateAccessToken_shouldContainTokenVersion() {
        String token = jwtService.generateAccessToken("john@example.com", "USER", 5);

        assertThat(jwtService.extractTokenVersion(token)).isEqualTo(5);
    }

    // ─── generateRefreshToken ─────────────────────────────────────

    @Test
    void generateRefreshToken_shouldBeRefreshToken() {
        String token = jwtService.generateRefreshToken("john@example.com", 0);

        assertThat(jwtService.isRefreshToken(token)).isTrue();
    }

    @Test
    void generateRefreshToken_shouldContainEmail() {
        String token = jwtService.generateRefreshToken("john@example.com", 0);

        assertThat(jwtService.extractEmail(token)).isEqualTo("john@example.com");
    }

    // ─── jti uniqueness ───────────────────────────────────────────

    @Test
    void generateTokens_shouldHaveUniqueJti() {
        String t1 = jwtService.generateAccessToken("john@example.com", "USER", 0);
        String t2 = jwtService.generateAccessToken("john@example.com", "USER", 0);

        assertThat(jwtService.extractJti(t1)).isNotEqualTo(jwtService.extractJti(t2));
    }

    // ─── isTokenValid ─────────────────────────────────────────────

    @Test
    void isTokenValid_shouldReturnFalse_whenEmailDoesNotMatch() {
        String token = jwtService.generateAccessToken("john@example.com", "USER", 0);

        assertThat(jwtService.isTokenValid(token, "other@example.com")).isFalse();
    }

    // ─── ttl helpers ──────────────────────────────────────────────

    @Test
    void getAccessTokenTtlSeconds_shouldReturnCorrectValue() {
        assertThat(jwtService.getAccessTokenTtlSeconds()).isEqualTo(ACCESS_EXP / 1000);
    }

    @Test
    void getRefreshTokenTtlSeconds_shouldReturnCorrectValue() {
        assertThat(jwtService.getRefreshTokenTtlSeconds()).isEqualTo(REFRESH_EXP / 1000);
    }
}