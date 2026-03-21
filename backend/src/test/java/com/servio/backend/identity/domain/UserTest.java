package com.servio.backend.identity.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    void debeConstruirseCorrectamente() {
        User user = User.builder()
                .id(1)
                .firstname("Juan")
                .lastname("Perez")
                .email("juan@test.com")
                .password("encoded_password")
                .role(Role.USER)
                .blocked(false)
                .build();

        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getFirstname()).isEqualTo("Juan");
        assertThat(user.getEmail()).isEqualTo("juan@test.com");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isBlocked()).isFalse();
    }

    @Test
    void debePermitirBloquearUsuario() {
        User user = User.builder()
                .id(1)
                .firstname("Juan")
                .lastname("Perez")
                .email("juan@test.com")
                .password("encoded_password")
                .role(Role.USER)
                .blocked(false)
                .build();

        user.setBlocked(true);

        assertThat(user.isBlocked()).isTrue();
    }
}