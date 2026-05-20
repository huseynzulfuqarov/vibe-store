package com.example.vibe_store.dto.grade;

import com.example.vibe_store.enums.TargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateGradeRuleRequestDTO(
        Integer positionId,

        @NotNull(message = "Target type (STORE_TARGET or EMPLOYEE_TARGET) must be selected")
        TargetType targetType,

        @Positive(message = "Fixed amount must be greater than 0")
        BigDecimal fixedAmount,

        @PositiveOrZero
        BigDecimal minThreshold,

        @PositiveOrZero
        BigDecimal maxThreshold,

        @Positive(message = "Percentage must be greater than 0")
        BigDecimal percentage,

        @Positive(message = "Share percentage must be greater than 0")
        BigDecimal sharePercentage
) {}