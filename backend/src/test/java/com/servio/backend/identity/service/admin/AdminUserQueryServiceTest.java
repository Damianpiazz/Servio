package com.servio.backend.identity.service.admin;

import com.servio.backend.identity.dto.request.*;
import com.servio.backend.identity.dto.response.UserResponse;
import com.servio.backend.identity.exception.EmailAlreadyExistsException;
import com.servio.backend.identity.model.Role;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import com.servio.backend.shared.exception.ResourceNotFoundException;
import org.jetbrains.annotations.NotNull;
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
class AdminUserQueryServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks AdminUserQueryService queryService;

    @Test
    void listUsers_shouldReturnMappedPage() {
        Page<@NotNull User> page = new PageImpl<>(List.of(user(1, "a@example.com")));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<@NotNull UserResponse> result = queryService.listUsers(new AdminUserFilterRequest(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("a@example.com");
    }

    @Test
    void getUserById_shouldReturnUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user(1, "a@example.com")));

        UserResponse response = queryService.getUserById(1);

        assertThat(response.getId()).isEqualTo(1);
    }

    @Test
    void getUserById_shouldThrow_whenNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> queryService.getUserById(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findUserOrThrow_shouldThrow_whenNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> queryService.findUserOrThrow(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void toResponse_shouldMapAllFields() {
        User u = user(1, "a@example.com");
        UserResponse r = queryService.toResponse(u);
        assertThat(r.getId()).isEqualTo(1);
        assertThat(r.getEmail()).isEqualTo("a@example.com");
        assertThat(r.getRole()).isEqualTo(Role.USER);
        assertThat(r.isBlocked()).isFalse();
    }

    private User user(int id, String email) {
        return User.builder().id(id).email(email).firstname("John").lastname("Doe")
                .password("enc").role(Role.USER).blocked(false).tokenVersion(0).build();
    }
}