package com.servio.backend.identity.facade;

import com.servio.backend.identity.dto.request.LoginRequest;
import com.servio.backend.identity.dto.request.RegisterRequest;
import com.servio.backend.identity.dto.response.AuthResponse;
import com.servio.backend.identity.service.auth.LoginService;
import com.servio.backend.identity.service.auth.LogoutService;
import com.servio.backend.identity.service.auth.RefreshTokenService;
import com.servio.backend.identity.service.auth.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final LoginService loginService;
    private final RegisterService registerService;
    private final LogoutService logoutService;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse login(LoginRequest request) {
        return loginService.login(request);
    }

    public AuthResponse register(RegisterRequest request) {
        return registerService.register(request);
    }

    public AuthResponse refresh(String token) {
        return refreshTokenService.refresh(token);
    }

    public void logout(String token) {
        logoutService.logout(token);
    }
}