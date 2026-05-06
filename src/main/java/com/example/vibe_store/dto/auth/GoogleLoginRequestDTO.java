package com.example.vibe_store.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequestDTO(
        @NotBlank(message = "ID token is required")
        String idToken
) {}
