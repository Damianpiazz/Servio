package com.servio.backend.identity.controller;

import com.servio.backend.identity.dto.response.UserLogoResponse;
import com.servio.backend.identity.facade.UserLogoFacade;
import com.servio.backend.identity.model.UserLogo;
import com.servio.backend.identity.security.UserPrincipal;
import com.servio.backend.shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/logos")
@RequiredArgsConstructor
public class UserLogoController {

    private final UserLogoFacade userLogoFacade;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<@NotNull ApiResponse<UserLogoResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserLogo logo = userLogoFacade.upload(file, principal.getUser());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(toResponse(logo)));
    }

    @GetMapping
    public ResponseEntity<@NotNull ApiResponse<List<UserLogoResponse>>> getHistory(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(userLogoFacade.getHistory(principal.getId())));
    }

    @GetMapping("/active")
    public ResponseEntity<@NotNull ApiResponse<UserLogoResponse>> getActive(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(userLogoFacade.getActive(principal.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<@NotNull ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        userLogoFacade.delete(id, principal.getUser());
        return ResponseEntity.ok(ApiResponse.ok("Logo deleted successfully", null));
    }

    private UserLogoResponse toResponse(UserLogo logo) {
        return UserLogoResponse.builder()
                .id(logo.getId())
                .url(logo.getUrl())
                .active(logo.isActive())
                .uploadedAt(logo.getUploadedAt())
                .build();
    }
}