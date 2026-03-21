package com.servio.backend.identity.infrastructure.web;

import com.servio.backend.identity.application.port.in.usecase.BlockUserUseCase;
import com.servio.backend.shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final BlockUserUseCase blockUserUseCase;

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> block(@PathVariable Integer id) {
        blockUserUseCase.block(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario bloqueado", null));
    }

    @PatchMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unblock(@PathVariable Integer id) {
        blockUserUseCase.unblock(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario desbloqueado", null));
    }
}