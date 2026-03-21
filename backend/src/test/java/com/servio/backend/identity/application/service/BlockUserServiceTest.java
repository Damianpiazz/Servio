package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.out.UserRepositoryPort;
import com.servio.backend.identity.domain.Role;
import com.servio.backend.identity.domain.User;
import com.servio.backend.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockUserServiceTest {

    @Mock private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private BlockUserService blockUserService;

    @Test
    void debeBloquearUsuario() {
        User user = buildUser(false);
        when(userRepositoryPort.findById(1)).thenReturn(Optional.of(user));

        blockUserService.block(1);

        assertThat(user.isBlocked()).isTrue();
        verify(userRepositoryPort, times(1)).save(user);
    }

    @Test
    void debeDesbloquearUsuario() {
        User user = buildUser(true);
        when(userRepositoryPort.findById(1)).thenReturn(Optional.of(user));

        blockUserService.unblock(1);

        assertThat(user.isBlocked()).isFalse();
        verify(userRepositoryPort, times(1)).save(user);
    }

    @Test
    void debeFallarSiUsuarioNoExiste() {
        when(userRepositoryPort.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blockUserService.block(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private User buildUser(boolean blocked) {
        return User.builder()
                .id(1).firstname("Juan").lastname("Perez")
                .email("juan@test.com").password("encoded")
                .role(Role.USER).blocked(blocked)
                .build();
    }
}