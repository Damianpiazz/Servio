package com.servio.backend.identity.infrastructure.web;

import com.servio.backend.identity.application.port.in.command.ChangePasswordCommand;
import com.servio.backend.identity.application.port.in.usecase.ChangePasswordUseCase;
import com.servio.backend.identity.application.port.in.usecase.GetUserLogosUseCase;
import com.servio.backend.identity.domain.User;
import com.servio.backend.identity.infrastructure.web.dto.ChangePasswordRequest;
import com.servio.backend.identity.infrastructure.web.dto.UserResponse;
import com.servio.backend.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final ChangePasswordUseCase changePasswordUseCase;
    private final GetUserLogosUseCase getUserLogosUseCase;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(
            @AuthenticationPrincipal User user
    ) {
        String activeLogoUrl = null;
        try {
            activeLogoUrl = getUserLogosUseCase.getActive(user.getId()).getUrl();
        } catch (Exception ignored) {}

        return ResponseEntity.ok(ApiResponse.ok(toResponse(user, activeLogoUrl)));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User user
    ) {
        changePasswordUseCase.changePassword(
                new ChangePasswordCommand(
                        request.getCurrentPassword(),
                        request.getNewPassword(),
                        request.getConfirmationPassword()
                ),
                user
        );
        return ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada correctamente", null));
    }

    private UserResponse toResponse(User user, String activeLogoUrl) {
        return UserResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .role(user.getRole())
                .blocked(user.isBlocked())
                .activeLogoUrl(activeLogoUrl)
                .build();
    }
}