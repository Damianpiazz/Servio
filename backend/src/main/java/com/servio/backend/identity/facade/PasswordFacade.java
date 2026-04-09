package com.servio.backend.identity.facade;

import com.servio.backend.identity.dto.request.ChangePasswordRequest;
import com.servio.backend.identity.dto.request.ResetPasswordRequest;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.service.password.ChangePasswordService;
import com.servio.backend.identity.service.password.ResetPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordFacade {

    private final ChangePasswordService changePasswordService;
    private final ResetPasswordService resetPasswordService;

    public void changePassword(ChangePasswordRequest request, User user) {
        changePasswordService.changePassword(request, user);
    }

    public void requestReset(String email) {
        resetPasswordService.requestReset(email);
    }

    public void reset(ResetPasswordRequest request) {
        resetPasswordService.reset(request);
    }
}