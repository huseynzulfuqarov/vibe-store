package com.example.vibe_store.dto.auth;

import java.time.LocalDateTime;

public record SessionDTO(
        Long id,
        String deviceInfo,
        String ipAddress,
        LocalDateTime createdAt,
        LocalDateTime lastUsedAt
) {}