package com.servio.backend.identity.infrastructure.web;

import com.servio.backend.identity.application.port.in.command.LoginCommand;
import com.servio.backend.identity.application.port.in.command.RegisterCommand;
import com.servio.backend.identity.application.port.in.usecase.LoginUseCase;
import com.servio.backend.identity.application.port.in.usecase.LogoutUseCase;
import com.servio.backend.identity.application.port.in.usecase.RefreshTokenUseCase;
import com.servio.backend.identity.application.port.in.usecase.RegisterUseCase;
import com.servio.backend.identity.domain.Token;
import com.servio.backend.identity.domain.exception.InvalidTokenException;
import com.servio.backend.identity.infrastructure.web.dto.AuthResponse;
import com.servio.backend.identity.infrastructure.web.dto.LoginRequest;
import com.servio.backend.identity.infrastructure.web.dto.RegisterRequest;
import com.servio.backend.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        Token token = registerUseCase.register(new RegisterCommand(
                request.getFirstname(),
                request.getLastname(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        ));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(toResponse(token)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        Token token = loginUseCase.login(new LoginCommand(
                request.getEmail(),
                request.getPassword()
        ));
        return ResponseEntity.ok(ApiResponse.ok(toResponse(token)));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException();
        }
        Token token = refreshTokenUseCase.refresh(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.ok(toResponse(token)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException();
        }
        logoutUseCase.logout(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.ok("Sesión cerrada correctamente", null));
    }

    private AuthResponse toResponse(Token token) {
        return new AuthResponse(token.getAccessToken(), token.getRefreshToken());
    }
}