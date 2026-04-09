package com.servio.backend.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servio.backend.identity.dto.request.LoginRequest;
import com.servio.backend.identity.dto.request.RegisterRequest;
import com.servio.backend.identity.dto.response.AuthResponse;
import com.servio.backend.identity.exception.InvalidCredentialsException;
import com.servio.backend.identity.exception.InvalidTokenException;
import com.servio.backend.identity.facade.AuthFacade;
import com.servio.backend.identity.filter.JwtAuthenticationFilter;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean AuthFacade authFacade;
    // Required by security auto-config even if filter is excluded
    @MockitoBean JwtService jwtService;
    @MockitoBean TokenBlacklistService tokenBlacklistService;

    // ─── register ─────────────────────────────────────────────────

    @Test
    @WithMockUser
    void register_shouldReturn201_whenRequestIsValid() throws Exception {
        RegisterRequest req = registerRequest("john@example.com", "Password1");
        when(authFacade.register(any())).thenReturn(new AuthResponse("access", "refresh"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").value("access"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.status").value(201));
    }

    @Test
    @WithMockUser
    void register_shouldReturn400_whenEmailIsInvalid() throws Exception {
        RegisterRequest req = registerRequest("not-an-email", "Password1");

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    @WithMockUser
    void register_shouldReturn400_whenPasswordIsTooShort() throws Exception {
        RegisterRequest req = registerRequest("john@example.com", "short");

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ─── login ────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void login_shouldReturn200_whenCredentialsAreValid() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("Password1");
        when(authFacade.login(any())).thenReturn(new AuthResponse("access", "refresh"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access"));
    }

    @Test
    @WithMockUser
    void login_shouldReturn401_whenCredentialsAreInvalid() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("WrongPass1");
        when(authFacade.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ─── refresh ──────────────────────────────────────────────────

    @Test
    @WithMockUser
    void refresh_shouldReturn200_whenTokenIsValid() throws Exception {
        when(authFacade.refresh("valid-refresh")).thenReturn(new AuthResponse("new-access", "new-refresh"));

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .with(csrf())
                        .header("Authorization", "Bearer valid-refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access"));
    }

    @Test
    @WithMockUser
    void refresh_shouldReturn401_whenAuthHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void refresh_shouldReturn401_whenAuthHeaderIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .with(csrf())
                        .header("Authorization", "Token not-bearer-format"))
                .andExpect(status().isUnauthorized());
    }

    // ─── logout ───────────────────────────────────────────────────

    @Test
    @WithMockUser
    void logout_shouldReturn200_whenTokenIsValid() throws Exception {
        doNothing().when(authFacade).logout("valid-token");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf())
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Session closed successfully"));
    }

    @Test
    @WithMockUser
    void logout_shouldReturn401_whenHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ─── helpers ──────────────────────────────────────────────────

    private RegisterRequest registerRequest(String email, String password) {
        RegisterRequest req = new RegisterRequest();
        req.setFirstname("John");
        req.setLastname("Doe");
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }
}