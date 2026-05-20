package com.example.vibe_store.dto.grade;

import com.example.vibe_store.enums.TargetType;

import java.math.BigDecimal;

public record GradeRuleRespondDTO(
        Integer gradeId,
        BigDecimal minThreshold,
        BigDecimal maxThreshold,
        BigDecimal fixedAmount,
        BigDecimal percentage,
        BigDecimal sharePercentage,
        String positionName,
        TargetType targetType
) {}