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
class AdminUserCreateServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AdminUserQueryService adminUserQueryService;
    @InjectMocks AdminUserCreateService createService;

    @Test
    void createUser_shouldSaveUserWithEncodedPassword() {
        CreateUserRequest req = createRequest("new@example.com", Role.MANAGER);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(adminUserQueryService.toResponse(any())).thenReturn(UserResponse.builder()
                .email("new@example.com").role(Role.MANAGER).build());

        UserResponse response = createService.createUser(req);

        assertThat(response.getEmail()).isEqualTo("new@example.com");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
        assertThat(captor.getValue().isBlocked()).isFalse();
    }

    @Test
    void createUser_shouldThrow_whenEmailExists() {
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        assertThatThrownBy(() -> createService.createUser(createRequest("existing@example.com", Role.USER)))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    private CreateUserRequest createRequest(String email, Role role) {
        CreateUserRequest req = new CreateUserRequest();
        req.setFirstname("John"); req.setLastname("Doe");
        req.setEmail(email); req.setPassword("Password1"); req.setRole(role);
        return req;
    }
}