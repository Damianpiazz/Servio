package com.servio.backend.identity.application.port.out;

public interface TokenProviderPort {
    String generateAccessToken(String email, String role);
    String generateRefreshToken(String email);
    String extractEmail(String token);
    String extractRole(String token);
    boolean isTokenValid(String token, String email);
    long getAccessTokenTtlSeconds();
}