package com.servio.backend.identity.controller;

import com.servio.backend.identity.dto.request.AdminChangePasswordRequest;
import com.servio.backend.identity.dto.request.AdminUserFilterRequest;
import com.servio.backend.identity.dto.request.ChangeUserRoleRequest;
import com.servio.backend.identity.dto.request.CreateUserRequest;
import com.servio.backend.identity.dto.response.UserResponse;
import com.servio.backend.identity.facade.AdminUserFacade;
import com.servio.backend.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserFacade adminUserFacade;

    @GetMapping
    public ResponseEntity<@NotNull ApiResponse<Page<UserResponse>>> listUsers(
            AdminUserFilterRequest filter,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminUserFacade.listUsers(filter, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NotNull ApiResponse<UserResponse>> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(adminUserFacade.getUserById(id)));
    }

    @PostMapping
    public ResponseEntity<@NotNull ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(adminUserFacade.createUser(request)));
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<@NotNull ApiResponse<Void>> block(@PathVariable Integer id) {
        adminUserFacade.blockUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User blocked", null));
    }

    @PatchMapping("/{id}/unblock")
    public ResponseEntity<@NotNull ApiResponse<Void>> unblock(@PathVariable Integer id) {
        adminUserFacade.unblockUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User unblocked", null));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<@NotNull ApiResponse<Void>> changeRole(
            @PathVariable Integer id,
            @Valid @RequestBody ChangeUserRoleRequest request
    ) {
        adminUserFacade.changeRole(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Role updated", null));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<@NotNull ApiResponse<Void>> changePassword(
            @PathVariable Integer id,
            @Valid @RequestBody AdminChangePasswordRequest request
    ) {
        adminUserFacade.changePassword(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Password updated", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<@NotNull ApiResponse<Void>> deleteUser(@PathVariable Integer id) {
        adminUserFacade.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }
}