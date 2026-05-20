package com.example.vibe_store.dto.employee;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ChangeJobDetailsRequestDTO(
        @NotNull(message = "Employee ID cannot be empty")
        Integer employeeId,

        Integer targetStoreId,
        Integer targetPositionId,

        @DecimalMin(value = "370", message = "Salary cannot be less than 370 azn")
        BigDecimal newSalary
) {}