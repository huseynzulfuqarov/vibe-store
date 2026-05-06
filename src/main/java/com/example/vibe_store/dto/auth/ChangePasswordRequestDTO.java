package com.example.vibe_store.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(
        @NotBlank(message = "Current password is required")
        String oldPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password must be at least 6 characters")
        String newPassword
) {}
