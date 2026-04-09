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
class UserUpdateServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserQueryService userQueryService;
    @InjectMocks UserUpdateService userUpdateService;

    @Test
    void updateProfile_shouldPersistNewNames() {
        User user = user();
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstname("Jane");
        req.setLastname("Smith");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userQueryService.toResponse(any(), isNull())).thenReturn(
                UserResponse.builder().firstname("Jane").lastname("Smith").build());

        userUpdateService.updateProfile(req, user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getFirstname()).isEqualTo("Jane");
        assertThat(captor.getValue().getLastname()).isEqualTo("Smith");
    }

    @Test
    void updateProfile_shouldDelegateToQueryService() {
        User user = user();
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstname("Jane");
        req.setLastname("Smith");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userUpdateService.updateProfile(req, user);

        verify(userQueryService).toResponse(any(User.class), isNull());
    }

    private User user() {
        return User.builder().id(1).firstname("John").lastname("Doe")
                .email("john@example.com").password("enc")
                .role(Role.USER).blocked(false).tokenVersion(0).build();
    }
}
