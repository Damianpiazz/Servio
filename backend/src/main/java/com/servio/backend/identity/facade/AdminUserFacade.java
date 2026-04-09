package com.servio.backend.identity.facade;

import com.servio.backend.identity.dto.request.AdminChangePasswordRequest;
import com.servio.backend.identity.dto.request.AdminUserFilterRequest;
import com.servio.backend.identity.dto.request.ChangeUserRoleRequest;
import com.servio.backend.identity.dto.request.CreateUserRequest;
import com.servio.backend.identity.dto.response.UserResponse;
import com.servio.backend.identity.service.admin.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserFacade {

    private final AdminUserQueryService adminUserQueryService;
    private final AdminUserCreateService adminUserCreateService;
    private final AdminUserBlockService adminUserBlockService;
    private final AdminUserRoleService adminUserRoleService;
    private final AdminUserPasswordService adminUserPasswordService;
    private final AdminUserDeleteService adminUserDeleteService;

    public Page<@NotNull UserResponse> listUsers(AdminUserFilterRequest filter, Pageable pageable) {
        return adminUserQueryService.listUsers(filter, pageable);
    }

    public UserResponse getUserById(Integer userId) {
        return adminUserQueryService.getUserById(userId);
    }

    public UserResponse createUser(CreateUserRequest request) {
        return adminUserCreateService.createUser(request);
    }

    public void blockUser(Integer userId) {
        adminUserBlockService.blockUser(userId);
    }

    public void unblockUser(Integer userId) {
        adminUserBlockService.unblockUser(userId);
    }

    public void changeRole(Integer userId, ChangeUserRoleRequest request) {
        adminUserRoleService.changeRole(userId, request);
    }

    public void changePassword(Integer userId, AdminChangePasswordRequest request) {
        adminUserPasswordService.changePassword(userId, request);
    }

    public void deleteUser(Integer userId) {
        adminUserDeleteService.deleteUser(userId);
    }
}