package com.servio.backend.identity.controller;

import com.servio.backend.identity.dto.request.LoginRequest;
import com.servio.backend.identity.dto.request.RegisterRequest;
import com.servio.backend.identity.dto.response.AuthResponse;
import com.servio.backend.identity.exception.InvalidTokenException;
import com.servio.backend.identity.facade.AuthFacade;
import com.servio.backend.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthFacade authFacade;

    @PostMapping("/register")
    public ResponseEntity<@NotNull ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(authFacade.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<@NotNull ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(authFacade.login(request)));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<@NotNull ApiResponse<AuthResponse>> refresh(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException();
        }
        return ResponseEntity.ok(ApiResponse.ok(authFacade.refresh(authHeader.substring(7))));
    }

    @PostMapping("/logout")
    public ResponseEntity<@NotNull ApiResponse<Void>> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException();
        }
        authFacade.logout(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.ok("Session closed successfully", null));
    }
}