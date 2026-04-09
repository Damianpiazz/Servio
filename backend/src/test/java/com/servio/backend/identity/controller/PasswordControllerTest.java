package com.servio.backend.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servio.backend.identity.dto.request.ChangePasswordRequest;
import com.servio.backend.identity.dto.request.ForgotPasswordRequest;
import com.servio.backend.identity.dto.request.ResetPasswordRequest;
import com.servio.backend.identity.facade.PasswordFacade;
import com.servio.backend.identity.filter.JwtAuthenticationFilter;
import com.servio.backend.identity.model.Role;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.security.UserPrincipal;
import com.servio.backend.identity.service.blacklist.TokenBlacklistService;
import com.servio.backend.identity.service.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PasswordController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
class PasswordControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean PasswordFacade passwordFacade;
    @MockitoBean JwtService jwtService;
    @MockitoBean TokenBlacklistService tokenBlacklistService;

    // ─── PATCH /me ────────────────────────────────────────────────

    @Test
    @WithMockUser
    void changePassword_shouldReturn200_whenRequestIsValid() throws Exception {
        doNothing().when(passwordFacade).changePassword(any(), any());

        mockMvc.perform(patch("/api/v1/password/me")
                        .with(csrf())
                        .with(user(principal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validChangeRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));
    }

    @Test
    @WithMockUser
    void changePassword_shouldReturn400_whenNewPasswordTooShort() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("OldPassword1");
        req.setNewPassword("short");
        req.setConfirmationPassword("short");

        mockMvc.perform(patch("/api/v1/password/me")
                        .with(csrf())
                        .with(user(principal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ─── POST /forgot ─────────────────────────────────────────────

    @Test
    @WithMockUser
    void forgotPassword_shouldReturn200_always() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("john@example.com");
        doNothing().when(passwordFacade).requestReset(any());

        mockMvc.perform(post("/api/v1/password/forgot")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email exists, you will receive recovery instructions"));
    }

    @Test
    @WithMockUser
    void forgotPassword_shouldReturn400_whenEmailIsInvalid() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/password/forgot")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ─── POST /reset ──────────────────────────────────────────────

    @Test
    @WithMockUser
    void resetPassword_shouldReturn200_whenRequestIsValid() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid-token");
        req.setNewPassword("NewPassword1");
        doNothing().when(passwordFacade).reset(any());

        mockMvc.perform(post("/api/v1/password/reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));
    }

    @Test
    @WithMockUser
    void resetPassword_shouldReturn400_whenTokenIsBlank() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("  ");
        req.setNewPassword("NewPassword1");

        mockMvc.perform(post("/api/v1/password/reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ─── helpers ──────────────────────────────────────────────────

    private UserPrincipal principal() {
        User user = User.builder()
                .id(1).email("john@example.com").firstname("John")
                .password("encoded").role(Role.USER)
                .blocked(false).tokenVersion(0).build();
        return new UserPrincipal(user);
    }

    private ChangePasswordRequest validChangeRequest() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("OldPassword1");
        req.setNewPassword("NewPassword1");
        req.setConfirmationPassword("NewPassword1");
        return req;
    }
}