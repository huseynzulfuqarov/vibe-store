package com.example.vibe_store.dto.grade;

import com.example.vibe_store.enums.TargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateGradeRuleRequestDTO {

    private Integer positionId;

    @NotNull(message = "Hədəf tipi (STORE_TARGET və ya EMPLOYEE_TARGET) seçilməlidir")
    private TargetType targetType;

    @PositiveOrZero
    private BigDecimal fixedAmount;

    @PositiveOrZero
    private BigDecimal minThreshold;

    @PositiveOrZero
    private BigDecimal maxThreshold;

    @PositiveOrZero
    private BigDecimal percentage;

    @PositiveOrZero
    private BigDecimal sharePercentage;
}