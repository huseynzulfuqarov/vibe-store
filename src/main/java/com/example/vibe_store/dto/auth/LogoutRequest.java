package com.example.vibe_store.dto.auth;

public record LogoutRequest(
        String refreshToken
) {}