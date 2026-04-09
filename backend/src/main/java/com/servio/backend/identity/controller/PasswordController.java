package com.servio.backend.identity.controller;

import com.servio.backend.identity.dto.request.ChangePasswordRequest;
import com.servio.backend.identity.dto.request.ForgotPasswordRequest;
import com.servio.backend.identity.dto.request.ResetPasswordRequest;
import com.servio.backend.identity.facade.PasswordFacade;
import com.servio.backend.identity.security.UserPrincipal;
import com.servio.backend.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordFacade passwordFacade;

    @PatchMapping("/me")
    public ResponseEntity<@NotNull ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        passwordFacade.changePassword(request, principal.getUser());
        return ResponseEntity.ok(ApiResponse.ok("Password updated successfully", null));
    }

    @PostMapping("/forgot")
    public ResponseEntity<@NotNull ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        passwordFacade.requestReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(
                "If the email exists, you will receive recovery instructions", null));
    }

    @PostMapping("/reset")
    public ResponseEntity<@NotNull ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordFacade.reset(request);
        return ResponseEntity.ok(ApiResponse.ok("Password updated successfully", null));
    }
}