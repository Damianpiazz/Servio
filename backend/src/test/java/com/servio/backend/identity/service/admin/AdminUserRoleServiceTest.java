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
class AdminUserRoleServiceTest {

    @Mock UserRepository userRepository;
    @Mock AdminUserQueryService adminUserQueryService;
    @InjectMocks AdminUserRoleService roleService;

    @Test
    void changeRole_shouldUpdateRole() {
        User user = User.builder().id(1).role(Role.USER).tokenVersion(0).build();
        ChangeUserRoleRequest req = new ChangeUserRoleRequest();
        req.setRole(Role.MANAGER);
        when(adminUserQueryService.findUserOrThrow(1)).thenReturn(user);

        roleService.changeRole(1, req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.MANAGER);
    }

    @Test
    void changeRole_shouldThrow_whenUserNotFound() {
        when(adminUserQueryService.findUserOrThrow(99))
                .thenThrow(new ResourceNotFoundException("User", "id", 99));
        assertThatThrownBy(() -> roleService.changeRole(99, new ChangeUserRoleRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}