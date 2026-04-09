package com.servio.backend.identity.controller;

import com.servio.backend.identity.dto.request.UpdateProfileRequest;
import com.servio.backend.identity.dto.response.UserResponse;
import com.servio.backend.identity.facade.UserFacade;
import com.servio.backend.identity.security.UserPrincipal;
import com.servio.backend.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;

    @GetMapping("/me")
    public ResponseEntity<@NotNull ApiResponse<UserResponse>> me(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(userFacade.getProfile(principal.getUser())));
    }

    @PatchMapping("/me")
    public ResponseEntity<@NotNull ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(userFacade.updateProfile(request, principal.getUser())));
    }
}