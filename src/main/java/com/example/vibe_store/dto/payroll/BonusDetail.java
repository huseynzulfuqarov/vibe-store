package com.example.vibe_store.dto.payroll;

import java.math.BigDecimal;

public record BonusDetail(
        Integer gradeId,
        String gradeName,
        BigDecimal bonusAmount
) {}