package com.servio.backend.identity.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Token {
    private final String accessToken;
    private final String refreshToken;
}