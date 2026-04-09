package com.servio.backend.identity.service.user;

import com.servio.backend.identity.dto.request.UpdateProfileRequest;
import com.servio.backend.identity.dto.response.UserResponse;
import com.servio.backend.identity.model.Role;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @InjectMocks UserQueryService userQueryService;

    @Test
    void toResponse_shouldMapAllFields() {
        User user = user();
        UserResponse r = userQueryService.toResponse(user, "http://cdn/logo.jpg");

        assertThat(r.getId()).isEqualTo(1);
        assertThat(r.getFirstname()).isEqualTo("John");
        assertThat(r.getLastname()).isEqualTo("Doe");
        assertThat(r.getEmail()).isEqualTo("john@example.com");
        assertThat(r.getRole()).isEqualTo(Role.USER);
        assertThat(r.isBlocked()).isFalse();
        assertThat(r.getActiveLogoUrl()).isEqualTo("http://cdn/logo.jpg");
    }

    @Test
    void toResponse_shouldAllowNullLogoUrl() {
        UserResponse r = userQueryService.toResponse(user(), null);
        assertThat(r.getActiveLogoUrl()).isNull();
    }

    private User user() {
        return User.builder().id(1).firstname("John").lastname("Doe")
                .email("john@example.com").password("enc")
                .role(Role.USER).blocked(false).tokenVersion(0).build();
    }
}