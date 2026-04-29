package com.example.vibe_store.dto.grade;

import com.example.vibe_store.enums.TargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateGradeRuleRequestDTO {

    private Integer positionId;

    @NotNull(message = "Target type (STORE_TARGET or EMPLOYEE_TARGET) must be selected")
    private TargetType targetType;

    @Positive(message = "Fixed amount must be greater than 0")
    private BigDecimal fixedAmount;

    @PositiveOrZero
    private BigDecimal minThreshold;

    @PositiveOrZero
    private BigDecimal maxThreshold;

    @Positive(message = "Percentage must be greater than 0")
    private BigDecimal percentage;

    @Positive(message = "Share percentage must be greater than 0")
    private BigDecimal sharePercentage;
}