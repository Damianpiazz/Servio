package com.servio.backend.identity.application.port.out;

public interface TokenBlacklistPort {
    void revoke(String token, long ttlSeconds);
    boolean isRevoked(String token);
}