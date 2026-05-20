package com.example.vibe_store.dto.employee;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AllEmployeeDetailsResponseDTO(
        Integer employeeId,
        String firstName,
        String lastName,
        LocalDateTime hireDate,
        LocalDateTime terminationDate,
        Byte age,
        String email,
        BigDecimal currentSalary,
        String currentPositionName,
        String currentStoreName
) {}