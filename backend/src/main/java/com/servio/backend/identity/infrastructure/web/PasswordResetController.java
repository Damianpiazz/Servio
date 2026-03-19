package com.servio.backend.identity.infrastructure.web;

import com.servio.backend.identity.application.port.in.command.ResetPasswordCommand;
import com.servio.backend.identity.application.port.in.usecase.ResetPasswordUseCase;
import com.servio.backend.identity.infrastructure.web.dto.PasswordChangeRequest;
import com.servio.backend.identity.infrastructure.web.dto.PasswordResetRequest;
import com.servio.backend.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final ResetPasswordUseCase resetPasswordUseCase;

    @PostMapping("/request-reset")
    public ResponseEntity<ApiResponse<Void>> requestReset(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        resetPasswordUseCase.requestReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("Correo de recuperación enviado", null));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> reset(
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        resetPasswordUseCase.reset(new ResetPasswordCommand(
                request.getToken(),
                request.getNewPassword()
        ));
        return ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada correctamente", null));
    }
}