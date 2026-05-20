package com.example.vibe_store.dto.employee;

import jakarta.validation.constraints.NotBlank;

public record CreatePositionRequestDTO(
        @NotBlank(message = "Position name cannot be blank")
        String positionName
) {}