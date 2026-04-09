package com.servio.backend.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servio.backend.identity.dto.request.*;
import com.servio.backend.identity.dto.response.UserResponse;
import com.servio.backend.identity.facade.AdminUserFacade;
import com.servio.backend.identity.filter.JwtAuthenticationFilter;
import com.servio.backend.identity.model.Role;
import com.servio.backend.identity.service.blacklist.TokenBlacklistService;
import com.servio.backend.identity.service.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminUserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
class AdminUserControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean AdminUserFacade adminUserFacade;
    @MockitoBean JwtService jwtService;
    @MockitoBean TokenBlacklistService tokenBlacklistService;

    // ─── GET / ────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void listUsers_shouldReturn200_withPagedUsers() throws Exception {
        when(adminUserFacade.listUsers(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(userResponse(1, "john@example.com"))));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("john@example.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listUsers_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    // ─── GET /{id} ────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_shouldReturn200() throws Exception {
        when(adminUserFacade.getUserById(1)).thenReturn(userResponse(1, "john@example.com"));

        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // ─── POST / ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldReturn201_whenRequestIsValid() throws Exception {
        CreateUserRequest req = createRequest("new@example.com", Role.USER);
        when(adminUserFacade.createUser(any())).thenReturn(userResponse(2, "new@example.com"));

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("new@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldReturn400_whenEmailIsInvalid() throws Exception {
        CreateUserRequest req = createRequest("bad-email", Role.USER);

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.email").exists());
    }

    // ─── PATCH /{id}/block ────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockUser_shouldReturn200() throws Exception {
        doNothing().when(adminUserFacade).blockUser(1);

        mockMvc.perform(patch("/api/v1/admin/users/1/block").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User blocked"));
    }

    // ─── PATCH /{id}/unblock ──────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void unblockUser_shouldReturn200() throws Exception {
        doNothing().when(adminUserFacade).unblockUser(1);

        mockMvc.perform(patch("/api/v1/admin/users/1/unblock").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User unblocked"));
    }

    // ─── PATCH /{id}/role ─────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeRole_shouldReturn200_whenRoleIsValid() throws Exception {
        ChangeUserRoleRequest req = new ChangeUserRoleRequest();
        req.setRole(Role.MANAGER);
        doNothing().when(adminUserFacade).changeRole(eq(1), any());

        mockMvc.perform(patch("/api/v1/admin/users/1/role")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role updated"));
    }

    // ─── PATCH /{id}/password ─────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void changePassword_shouldReturn200_whenPasswordIsValid() throws Exception {
        AdminChangePasswordRequest req = new AdminChangePasswordRequest();
        req.setNewPassword("NewPassword1");
        doNothing().when(adminUserFacade).changePassword(eq(1), any());

        mockMvc.perform(patch("/api/v1/admin/users/1/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated"));
    }

    // ─── DELETE /{id} ─────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_shouldReturn200() throws Exception {
        doNothing().when(adminUserFacade).deleteUser(1);

        mockMvc.perform(delete("/api/v1/admin/users/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted"));
    }

    // ─── helpers ──────────────────────────────────────────────────

    private UserResponse userResponse(int id, String email) {
        return UserResponse.builder()
                .id(id).firstname("John").lastname("Doe")
                .email(email).role(Role.USER).blocked(false).build();
    }

    private CreateUserRequest createRequest(String email, Role role) {
        CreateUserRequest req = new CreateUserRequest();
        req.setFirstname("John");
        req.setLastname("Doe");
        req.setEmail(email);
        req.setPassword("Password1");
        req.setRole(role);
        return req;
    }
}