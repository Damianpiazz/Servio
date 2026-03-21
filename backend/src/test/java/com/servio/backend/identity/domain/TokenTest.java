package com.servio.backend.identity.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class TokenTest {

    @Test
    void debeConstruirseCorrectamente() {
        Token token = Token.builder()
                .accessToken("access.token.jwt")
                .refreshToken("refresh.token.jwt")
                .build();

        assertThat(token.getAccessToken()).isEqualTo("access.token.jwt");
        assertThat(token.getRefreshToken()).isEqualTo("refresh.token.jwt");
    }
}