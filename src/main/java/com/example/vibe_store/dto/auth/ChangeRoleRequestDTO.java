package com.example.vibe_store.dto.auth;

import com.example.vibe_store.enums.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequestDTO(
        @NotNull(message = "Employee ID is required")
        Integer employeeId,

        @NotNull(message = "New role is required")
        Role newRole
) {}
