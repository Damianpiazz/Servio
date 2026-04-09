package com.servio.backend.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servio.backend.identity.dto.request.UpdateProfileRequest;
import com.servio.backend.identity.dto.response.UserResponse;
import com.servio.backend.identity.facade.UserFacade;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean UserFacade userFacade;
    @MockitoBean JwtService jwtService;
    @MockitoBean TokenBlacklistService tokenBlacklistService;

    // ─── GET /me ──────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getMe_shouldReturn200_withUserData() throws Exception {
        UserResponse response = userResponse();
        when(userFacade.getProfile(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me")
                        .with(user(principal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("john@example.com"))
                .andExpect(jsonPath("$.data.firstname").value("John"));
    }

    @Test
    void getMe_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ─── PATCH /me ────────────────────────────────────────────────

    @Test
    @WithMockUser
    void updateProfile_shouldReturn200_whenRequestIsValid() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstname("Jane");
        req.setLastname("Smith");

        UserResponse updated = UserResponse.builder()
                .id(1).firstname("Jane").lastname("Smith")
                .email("john@example.com").role(Role.USER).build();

        when(userFacade.updateProfile(any(), any())).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/users/me")
                        .with(csrf())
                        .with(user(principal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstname").value("Jane"))
                .andExpect(jsonPath("$.data.lastname").value("Smith"));
    }

    @Test
    @WithMockUser
    void updateProfile_shouldReturn400_whenFirstnameIsBlank() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstname("");
        req.setLastname("Smith");

        mockMvc.perform(patch("/api/v1/users/me")
                        .with(csrf())
                        .with(user(principal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.firstname").exists());
    }

    // ─── helpers ──────────────────────────────────────────────────

    private UserPrincipal principal() {
        User user = User.builder()
                .id(1).email("john@example.com").firstname("John")
                .lastname("Doe").password("encoded").role(Role.USER)
                .blocked(false).tokenVersion(0).build();
        return new UserPrincipal(user);
    }

    private UserResponse userResponse() {
        return UserResponse.builder()
                .id(1).firstname("John").lastname("Doe")
                .email("john@example.com").role(Role.USER)
                .blocked(false).build();
    }
}