package com.servio.backend.identity.service.admin;

import com.servio.backend.identity.dto.request.*;
import com.servio.backend.identity.dto.response.UserResponse;
import com.servio.backend.identity.exception.EmailAlreadyExistsException;
import com.servio.backend.identity.model.Role;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import com.servio.backend.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserBlockServiceTest {

    @Mock UserRepository userRepository;
    @Mock AdminUserQueryService adminUserQueryService;
    @InjectMocks AdminUserBlockService blockService;

    @Test
    void blockUser_shouldSetBlockedAndIncrementTokenVersion() {
        User user = user(1);
        when(adminUserQueryService.findUserOrThrow(1)).thenReturn(user);

        blockService.blockUser(1);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isBlocked()).isTrue();
        assertThat(captor.getValue().getTokenVersion()).isEqualTo(1);
    }

    @Test
    void unblockUser_shouldClearBlockedFlag() {
        User user = user(1);
        user.setBlocked(true);
        when(adminUserQueryService.findUserOrThrow(1)).thenReturn(user);

        blockService.unblockUser(1);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isBlocked()).isFalse();
    }

    @Test
    void blockUser_shouldThrow_whenUserNotFound() {
        when(adminUserQueryService.findUserOrThrow(99))
                .thenThrow(new ResourceNotFoundException("User", "id", 99));
        assertThatThrownBy(() -> blockService.blockUser(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private User user(int id) {
        return User.builder().id(id).email("u@example.com").password("enc")
                .role(Role.USER).blocked(false).tokenVersion(0).build();
    }
}
