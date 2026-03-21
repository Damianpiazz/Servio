package com.servio.backend.identity.infrastructure.web;

import com.servio.backend.identity.application.port.in.usecase.DeleteUserLogoUseCase;
import com.servio.backend.identity.application.port.in.usecase.GetUserLogosUseCase;
import com.servio.backend.identity.application.port.in.usecase.UploadUserLogoUseCase;
import com.servio.backend.identity.domain.User;
import com.servio.backend.identity.domain.UserLogo;
import com.servio.backend.identity.infrastructure.web.dto.UserLogoResponse;
import com.servio.backend.shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
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

    private final UploadUserLogoUseCase uploadUserLogoUseCase;
    private final DeleteUserLogoUseCase deleteUserLogoUseCase;
    private final GetUserLogosUseCase getUserLogosUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserLogoResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user
    ) {
        UserLogo logo = uploadUserLogoUseCase.upload(file, user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(toResponse(logo)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserLogoResponse>>> getHistory(
            @AuthenticationPrincipal User user
    ) {
        List<UserLogoResponse> logos = getUserLogosUseCase.getHistory(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(logos));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<UserLogoResponse>> getActive(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(toResponse(getUserLogosUseCase.getActive(user.getId())))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        deleteUserLogoUseCase.delete(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Logo eliminado correctamente", null));
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