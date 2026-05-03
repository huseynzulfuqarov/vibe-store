package com.example.vibe_store.dto.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}
